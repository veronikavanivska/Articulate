package org.example.slots.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.slots.clients.ArticleClient;
import org.example.slots.clients.ArticleSlotsClient;
import org.example.slots.clients.ProfilesClient;
import org.example.slots.entities.PublicationKind;
import org.example.slots.entities.SlotComputation;
import org.example.slots.entities.SlotDraft;
import org.example.slots.entities.SlotDraftItem;
import org.example.slots.helpers.SlotComute;
import org.example.slots.helpers.SlotMapper;
import org.example.slots.helpers.SyncHelper;
import org.example.slots.repositories.SlotDraftItemRepository;
import org.example.slots.repositories.SlotDraftRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.example.slots.helpers.SlotComute.isMono;
import static org.example.slots.helpers.SlotMapper.*;

@Service
public class SlotService extends SlotServiceGrpc.SlotServiceImplBase {

    private final SlotDraftRepository draftRepo;
    private final SlotDraftItemRepository itemRepo;
    private final SyncHelper syncHelper;

    public SlotService(SyncHelper syncHelper, SlotDraftRepository draftRepo, SlotDraftItemRepository itemRepo) {
        this.draftRepo = draftRepo;
        this.itemRepo = itemRepo;
        this.syncHelper = syncHelper;
    }

    // =========================================================
    // gRPC endpoints
    // =========================================================

    @Override
    @Transactional
    public void addToActiveSlot(AddToSlotRequest request, StreamObserver<DraftView> responseObserver) {
        try {
            CycleItem active = requireActiveCycle();

            long userId = request.getUserId();
            long disciplineId = request.getDisciplineId();
            long itemId = request.getItemId();
            long cycleId = active.getId();
            int evalYear = active.getActiveYear();

            validateIds(userId, disciplineId, itemId);

            PublicationKind kind = toKind(request.getItemType());

            ItemForSlots item = ArticleSlotsClient.getItemForSlots(userId, request.getItemType(), itemId);
            requireYear(item, evalYear);

            WorkerStatement ownerSt = ProfilesClient.getOrCreateStatement(userId, disciplineId, evalYear).getStatement();

            BigDecimal maxSlots = SlotComute.bd(ownerSt.getMaxSlots(), SlotComute.SCALE_SLOT);
            BigDecimal maxMonoSlots = SlotComute.bd(ownerSt.getMaxMonoSlots(), SlotComute.SCALE_SLOT);

            SlotDraft draft = getOrCreateDraft(userId, disciplineId, cycleId, evalYear, maxSlots, maxMonoSlots);

            // idempotent
            if (itemRepo.existsByDraft_IdAndKindAndPublicationId(draft.getId(), kind, itemId)) {
                responseObserver.onNext(loadDraftView(draft, active));
                responseObserver.onCompleted();
                return;
            }

            // compute slotValue + pointsRecalc
            SlotComputation comp = SlotComute.computeSlotValueAndPointsRecalc(
                    userId, disciplineId, evalYear, kind, item, ownerSt
            );

            // total limit
            enforceTotalLimit(draft, comp.slotValue());

            // mono sublimit
            if (isMono(kind)) {
                enforceMonoLimit(draft, comp.slotValue());
            }

            // save item
            saveDraftItem(draft, kind, item, itemId, comp);

            responseObserver.onNext(loadDraftView(draft, active));
            responseObserver.onCompleted();

        } catch (StatusRuntimeException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
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
            responseObserver.onNext(emptyDraftView(userId, disciplineId, active, cycleId, evalYear));
            responseObserver.onCompleted();
            return;
        }

        itemRepo.deleteByDraft_IdAndKindAndPublicationId(draft.getId(), kind, request.getItemId());

        responseObserver.onNext(loadDraftView(draft, active));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void getDraft(GetDraftRequest request, StreamObserver<DraftView> responseObserver) {

        CycleItem active = ArticleClient.getActiveEvalCycle();

        long ctxCycleId = request.getCycleId() > 0 ? request.getCycleId() : active.getId();
        int ctxEvalYear = request.getEvalYear() > 0 ? request.getEvalYear() : active.getActiveYear();

        SlotDraft draft = draftRepo.findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(
                request.getUserId(), ctxCycleId, request.getDisciplineId(), ctxEvalYear
        ).orElse(null);

        if (draft == null) {
            responseObserver.onNext(emptyDraftView(request.getUserId(), request.getDisciplineId(), active, ctxCycleId, ctxEvalYear));
            responseObserver.onCompleted();
            return;
        }

        responseObserver.onNext(loadDraftView(draft, active));
        responseObserver.onCompleted();
    }


    @Override
    public void syncSlotItem(SyncSlotItemRequest request, StreamObserver<SyncSlotItemResponse> responseObserver) {
        try {
            PublicationKind kind = toKind(request.getItemType());

            int affected = syncHelper.sync(
                    request.getAction(),
                    request.getItemType(),
                    kind,
                    request.getItemId()
            );

            responseObserver.onNext(SyncSlotItemResponse.newBuilder()
                    .setAffected(affected)
                    .setResponse(ApiResponse.newBuilder().setCode(200).setMessage("OK").build())
                    .build());
            responseObserver.onCompleted();

        } catch (StatusRuntimeException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    private DraftView loadDraftView(SlotDraft draft, CycleItem active) {
        List<SlotDraftItem> items = itemRepo.findByDraft_IdOrderByIdAsc(draft.getId());

        BigDecimal usedSlots = SlotComute.nz(itemRepo.sumSlotValue(draft.getId()));
        BigDecimal sumPoints = SlotComute.nz(itemRepo.sumPoints(draft.getId()));
        BigDecimal sumPointsRecalc = SlotComute.nz(itemRepo.sumPointsRecalc(draft.getId()));

        return toDraftView(draft, active, items, usedSlots, sumPoints, sumPointsRecalc);
    }

    private DraftView emptyDraftView(long userId, long disciplineId, CycleItem active, long ctxCycleId, int ctxEvalYear) {
        boolean editable = active.getIsActive()
                && active.getId() == ctxCycleId
                && active.getActiveYear() == ctxEvalYear;

        double maxSlots = 0.0;
        try {
            WorkerStatement st = ProfilesClient.getOrCreateStatement(userId, disciplineId, ctxEvalYear).getStatement();
            maxSlots = st.getMaxSlots();
        } catch (Exception ignore) {}

        return toEmptyDraftView(userId, disciplineId, ctxCycleId, ctxEvalYear, editable, maxSlots);
    }

    private void enforceTotalLimit(SlotDraft draft, BigDecimal slotValue) {
        BigDecimal usedNow = SlotComute.nz(itemRepo.sumSlotValue(draft.getId()));
        BigDecimal usedAfter = usedNow.add(slotValue);

        if (usedAfter.compareTo(SlotComute.nz(draft.getMaxSlots())) > 0) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Slot limit exceeded. usedAfter=" + usedAfter + " max=" + draft.getMaxSlots())
                    .asRuntimeException();
        }
    }

    private void enforceMonoLimit(SlotDraft draft, BigDecimal slotValue) {
        BigDecimal usedMonoNow = SlotComute.nz(itemRepo.sumSlotValueMono(draft.getId()));
        BigDecimal usedMonoAfter = usedMonoNow.add(slotValue);

        if (usedMonoAfter.compareTo(SlotComute.nz(draft.getMaxMonoSlots())) > 0) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Mono sublimit exceeded. usedMonoAfter=" + usedMonoAfter
                            + " maxMono=" + draft.getMaxMonoSlots())
                    .asRuntimeException();
        }
    }


    private void saveDraftItem(SlotDraft draft, PublicationKind kind, ItemForSlots item, long itemId, SlotComputation comp) {
        try {
            SlotDraftItem di = new SlotDraftItem();
            di.setDraft(draft);
            di.setPublicationId(itemId);
            di.setKind(kind);
            di.setPublicationYear(item.getPublicationYear());
            di.setTitle(item.getTitle() == null ? "" : item.getTitle());
            di.setPoints(comp.points());
            di.setSlotValue(comp.slotValue());
            di.setPointsRecalc(comp.pointsRecalc());

            itemRepo.save(di);
        } catch (DataIntegrityViolationException ignore) {

        }
    }

    private CycleItem requireActiveCycle() {
        CycleItem active = ArticleClient.getActiveEvalCycle();
        if (!active.getIsActive() || active.getActiveYear() <= 0) {
            throw Status.FAILED_PRECONDITION.withDescription("No active eval cycle / activeYear.").asRuntimeException();
        }
        return active;
    }

    private void validateIds(long userId, long disciplineId, long itemId) {
        if (userId <= 0 || disciplineId <= 0 || itemId <= 0) {
            throw Status.INVALID_ARGUMENT.withDescription("userId/disciplineId/itemId must be positive.").asRuntimeException();
        }
    }

    private void requireYear(ItemForSlots item, int evalYear) {
        if (item.getPublicationYear() != evalYear) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Only publications from activeYear=" + evalYear + " can be added.")
                    .asRuntimeException();
        }
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
                        return draftRepo.findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(userId, cycleId, disciplineId, evalYear)
                                .orElseThrow(() -> e);
                    }
                });
    }
}
