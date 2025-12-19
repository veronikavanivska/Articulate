package org.example.slots.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.slots.clients.ArticleClient;
import org.example.slots.clients.ArticleSlotsClient;
import org.example.slots.clients.ProfilesClient;
import org.example.slots.entities.PublicationKind;
import org.example.slots.entities.SlotDraft;
import org.example.slots.entities.SlotDraftItem;
import org.example.slots.repositories.SlotDraftItemRepository;
import org.example.slots.repositories.SlotDraftRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SlotServiceImpl extends SlotServiceGrpc.SlotServiceImplBase {

    private static final int SCALE_SLOT = 6;
    private static final int SCALE_POINTS = 4;

    private final SlotDraftRepository draftRepo;
    private final SlotDraftItemRepository itemRepo;

    public SlotServiceImpl(SlotDraftRepository draftRepo, SlotDraftItemRepository itemRepo) {
        this.draftRepo = draftRepo;
        this.itemRepo = itemRepo;
    }

    @Override
    @Transactional
    public void addToActiveSlot(AddToSlotRequest request, StreamObserver<DraftView> responseObserver) {

        CycleItem active = requireActiveCycle();

        long userId = request.getUserId();
        long disciplineId = request.getDisciplineId();
        long cycleId = active.getId();
        int evalYear = active.getActiveYear();

        if (userId <= 0 || disciplineId <= 0 || request.getItemId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("userId/disciplineId/itemId must be positive.")
                    .asRuntimeException());
            return;
        }

        PublicationKind kind = toKind(request.getItemType());

        // 1) Pobierz dane publikacji pod sloty (z article-service)
        ItemForSlots item;
        try {
            item = ArticleSlotsClient.getItemForSlots(userId, request.getItemType(), request.getItemId());
        } catch (StatusRuntimeException e) {
            responseObserver.onError(e);
            return;
        }

        // 2) Walidacja: tylko publikacje z activeYear
        if (item.getPublicationYear() != evalYear) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Only publications from activeYear=" + evalYear + " can be added.")
                    .asRuntimeException());
            return;
        }

        // 3) Statement ownera (limity slotów)
        WorkerStatement ownerSt;
        try {
            ownerSt = ProfilesClient.getOrCreateStatement(userId, disciplineId, evalYear).getStatement();
        } catch (StatusRuntimeException e) {
            // np. worker nieprzypisany do dyscypliny
            responseObserver.onError(e);
            return;
        }

        BigDecimal maxSlots = bd(ownerSt.getMaxSlots(), SCALE_SLOT);
        BigDecimal maxMonoSlots = bd(ownerSt.getMaxMonoSlots(), SCALE_SLOT);

        // 4) Find-or-create draft (z obsługą wyścigu na UNIQUE)
        SlotDraft draft = getOrCreateDraft(userId, disciplineId, cycleId, evalYear, maxSlots, maxMonoSlots);

        // 5) Idempotent: jeśli już jest – zwróć widok
        if (itemRepo.existsByDraft_IdAndKindAndPublicationId(draft.getId(), kind, request.getItemId())) {
            responseObserver.onNext(buildDraftView(draft, active));
            responseObserver.onCompleted();
            return;
        }

        // 6) Policzenie slotValue i pointsRecalc
        int N = item.getAuthorsCount();
        if (N <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Publication has no authors.")
                    .asRuntimeException());
            return;
        }

        BigDecimal sumSlotInDisc = BigDecimal.ZERO;

        for (SlotAuthor a : item.getAuthorsList()) {
            if (a.getUserId() <= 0) continue; // external author

            try {
                WorkerStatement st = ProfilesClient.getOrCreateStatement(a.getUserId(), disciplineId, evalYear).getStatement();
                sumSlotInDisc = sumSlotInDisc.add(bd(st.getSlotInDiscipline(), SCALE_SLOT));
            } catch (StatusRuntimeException e) {
                // W praktyce: internal user nie ma przypisanej tej dyscypliny → traktujemy jak "nie liczy się"
                // (możesz tu zamiast tego zablokować dodanie, jeśli chcesz ostrzej)
            }
        }

        // slotValue = (suma slotInDiscipline internal) / (liczba wszystkich autorów)
        BigDecimal slotValue = sumSlotInDisc
                .divide(BigDecimal.valueOf(N), SCALE_SLOT, RoundingMode.HALF_UP);

        // Jeśli slotValue = 0, to publikacja nie konsumuje slotów → to psuje system (można dodać nieskończenie wiele)
        if (slotValue.compareTo(BigDecimal.ZERO) <= 0) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Computed slotValue=0. Check coauthors' statements for this discipline/year (slotInDiscipline).")
                    .asRuntimeException());
            return;
        }

        BigDecimal points = bd(item.getPoints(), SCALE_POINTS);
        BigDecimal pointsRecalc = points.multiply(slotValue).setScale(SCALE_POINTS, RoundingMode.HALF_UP);

        // 7) Limit (maxSlots)
        BigDecimal usedNow = nz(itemRepo.sumSlotValue(draft.getId()));
        BigDecimal usedAfter = usedNow.add(slotValue);

        if (usedAfter.compareTo(draft.getMaxSlots()) > 0) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Slot limit exceeded. usedAfter=" + usedAfter + " max=" + draft.getMaxSlots())
                    .asRuntimeException());
            return;
        }

        // (opcjonalnie) Limit mono
        // jeśli chcesz, żeby MONOGRAPH/CHAPTER liczyły się też do maxMonoSlots:
        // if (kind == PublicationKind.MONOGRAPH || kind == PublicationKind.CHAPTER) { ... }

        // 8) Zapis itemu (z obsługą wyścigu)
        try {
            SlotDraftItem di = new SlotDraftItem();
            di.setDraft(draft);
            di.setPublicationId(request.getItemId());
            di.setKind(kind);
            di.setPublicationYear(item.getPublicationYear());
            di.setTitle(item.getTitle() == null ? "" : item.getTitle());
            di.setPoints(points);
            di.setSlotValue(slotValue);
            di.setPointsRecalc(pointsRecalc);

            itemRepo.save(di);
        } catch (DataIntegrityViolationException e) {
            // drugi insert w tym samym czasie → traktuj jako idempotent
        }

        responseObserver.onNext(buildDraftView(draft, active));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void removeFromActiveSlot(RemoveFromSlotRequest request, StreamObserver<DraftView> responseObserver) {

        CycleItem active = requireActiveCycle();

        long userId = request.getUserId();
        long disciplineId = request.getDisciplineId();
        long cycleId = active.getId();
        int evalYear = active.getActiveYear();

        PublicationKind kind = toKind(request.getItemType());

        SlotDraft draft = draftRepo.findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(
                userId, cycleId, disciplineId, evalYear
        ).orElse(null);

        if (draft == null) {
            responseObserver.onNext(emptyDraftView(userId, disciplineId, active));
            responseObserver.onCompleted();
            return;
        }

        itemRepo.deleteByDraft_IdAndKindAndPublicationId(draft.getId(), kind, request.getItemId());

        responseObserver.onNext(buildDraftView(draft, active));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void getDraft(GetDraftRequest request, StreamObserver<DraftView> responseObserver) {

        CycleItem active = ArticleClient.getActiveEvalCycle(); // tu nie wymuszamy, bo to może być podgląd

        long ctxCycleId = request.getCycleId() > 0 ? request.getCycleId() : active.getId();
        int ctxEvalYear = request.getEvalYear() > 0 ? request.getEvalYear() : active.getActiveYear();

        SlotDraft draft = draftRepo.findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(
                request.getUserId(), ctxCycleId, request.getDisciplineId(), ctxEvalYear
        ).orElse(null);

        if (draft == null) {
            // pokaż pusty widok, ale limit możesz pobrać ze statementu (żeby UI miało 1.0 zamiast 0)
            DraftView empty = emptyDraftView(request.getUserId(), request.getDisciplineId(),
                    active, ctxCycleId, ctxEvalYear);
            responseObserver.onNext(empty);
            responseObserver.onCompleted();
            return;
        }

        // UWAGA: editable liczymy względem AKTYWNEGO cyklu/roku, nie ctx
        responseObserver.onNext(buildDraftView(draft, active));
        responseObserver.onCompleted();
    }

    // ---------------- helpers ----------------

    private CycleItem requireActiveCycle() {
        CycleItem active = ArticleClient.getActiveEvalCycle();
        if (!active.getIsActive() || active.getActiveYear() <= 0) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("No active eval cycle / activeYear.")
                    .asRuntimeException();
        }
        return active;
    }

    private SlotDraft getOrCreateDraft(long userId, long disciplineId, long cycleId, int evalYear,
                                       BigDecimal maxSlots, BigDecimal maxMonoSlots) {

        return draftRepo.findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(userId, cycleId, disciplineId, evalYear)
                .orElseGet(() -> {
                    try {
                        SlotDraft d = new SlotDraft();
                        d.setUserId(userId);
                        d.setEvalCycleId(cycleId);
                        d.setDisciplineId(disciplineId);
                        d.setEvalYear(evalYear);
                        d.setMaxSlots(maxSlots);
                        d.setMaxMonoSlots(maxMonoSlots);
                        return draftRepo.save(d);
                    } catch (DataIntegrityViolationException e) {
                        // ktoś równolegle stworzył draft → pobierz
                        return draftRepo.findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(userId, cycleId, disciplineId, evalYear)
                                .orElseThrow(() -> e);
                    }
                });
    }

    private DraftView emptyDraftView(long userId, long disciplineId, CycleItem active) {
        return emptyDraftView(userId, disciplineId, active, active.getId(), active.getActiveYear());
    }

    private DraftView emptyDraftView(long userId, long disciplineId, CycleItem active, long ctxCycleId, int ctxEvalYear) {
        boolean editable = isEditable(active, ctxCycleId, ctxEvalYear);

        double maxSlots = 0.0;
        double maxMonoSlots = 0.0;

        // limit do UI (opcjonalnie, ale praktyczne)
        try {
            WorkerStatement st = ProfilesClient.getOrCreateStatement(userId, disciplineId, ctxEvalYear).getStatement();
            maxSlots = st.getMaxSlots();
            maxMonoSlots = st.getMaxMonoSlots();
        } catch (Exception ignore) {
            // jak się nie da pobrać statementu (np. brak przypisania), zostaw 0
        }

        return DraftView.newBuilder()
                .setDraftId(0)
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setCycleId(ctxCycleId)
                .setEvalYear(ctxEvalYear)
                .setEditable(editable)
                .setMaxSlots(maxSlots)
                .setUsedSlots(0)
                .setFreeSlots(maxSlots)
                .setSumPoints(0)
                .setSumPointsRecalc(0)
                .build();
    }

    private DraftView buildDraftView(SlotDraft draft, CycleItem active) {

        boolean editable = isEditable(active, draft.getEvalCycleId(), draft.getEvalYear());

        List<SlotDraftItem> items = itemRepo.findByDraft_IdOrderByIdAsc(draft.getId());

        BigDecimal usedSlots = nz(itemRepo.sumSlotValue(draft.getId()));
        BigDecimal sumPoints = nz(itemRepo.sumPoints(draft.getId()));
        BigDecimal sumPointsRecalc = nz(itemRepo.sumPointsRecalc(draft.getId()));

        BigDecimal maxSlots = draft.getMaxSlots() == null ? BigDecimal.ZERO : draft.getMaxSlots();
        BigDecimal free = maxSlots.subtract(usedSlots);
        if (free.compareTo(BigDecimal.ZERO) < 0) free = BigDecimal.ZERO;

        DraftView.Builder b = DraftView.newBuilder()
                .setDraftId(draft.getId())
                .setUserId(draft.getUserId())
                .setDisciplineId(draft.getDisciplineId())
                .setCycleId(draft.getEvalCycleId())
                .setEvalYear(draft.getEvalYear())
                .setEditable(editable)
                .setMaxSlots(maxSlots.doubleValue())
                .setUsedSlots(usedSlots.doubleValue())
                .setFreeSlots(free.doubleValue())
                .setSumPoints(sumPoints.doubleValue())
                .setSumPointsRecalc(sumPointsRecalc.doubleValue());

        for (SlotDraftItem it : items) {
            b.addItems(DraftItem.newBuilder()
                    .setItemType(fromKind(it.getKind()))
                    .setItemId(it.getPublicationId())
                    .setPublicationYear(it.getPublicationYear())
                    .setTitle(it.getTitle() == null ? "" : it.getTitle())
                    .setPoints(nz(it.getPoints()).doubleValue())
                    .setSlotValue(nz(it.getSlotValue()).doubleValue())
                    .setPointsRecalc(nz(it.getPointsRecalc()).doubleValue())
                    .build());
        }

        return b.build();
    }

    private boolean isEditable(CycleItem active, long ctxCycleId, int ctxEvalYear) {
        return active.getIsActive()
                && active.getId() == ctxCycleId
                && active.getActiveYear() == ctxEvalYear;
    }

    private SlotItemType fromKind(PublicationKind k) {
        return switch (k) {
            case ARTICLE -> SlotItemType.SLOT_ITEM_ARTICLE;
            case MONOGRAPH -> SlotItemType.SLOT_ITEM_MONOGRAPH;
            case CHAPTER -> SlotItemType.SLOT_ITEM_CHAPTER;
        };
    }

    private PublicationKind toKind(SlotItemType t) {
        return switch (t) {
            case SLOT_ITEM_ARTICLE -> PublicationKind.ARTICLE;
            case SLOT_ITEM_MONOGRAPH -> PublicationKind.MONOGRAPH;
            case SLOT_ITEM_CHAPTER -> PublicationKind.CHAPTER;
            default -> throw new IllegalArgumentException("Unsupported itemType: " + t);
        };
    }

    private static BigDecimal bd(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
