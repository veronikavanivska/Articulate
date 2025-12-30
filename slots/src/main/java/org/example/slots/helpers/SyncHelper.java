package org.example.slots.helpers;

import com.example.generated.ItemForSlots;
import com.example.generated.SlotAuthor;
import com.example.generated.SlotItemType;
import com.example.generated.SlotSyncAction;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.example.slots.clients.ArticleSlotsClient;
import org.example.slots.clients.ProfilesClient;
import org.example.slots.entities.PublicationKind;
import org.example.slots.entities.SlotComputation;
import org.example.slots.entities.SlotDraft;
import org.example.slots.entities.SlotDraftItem;
import org.example.slots.repositories.SlotDraftItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.slots.helpers.SlotComute.isMono;

@Service
public class SyncHelper {
    private final SlotDraftItemRepository itemRepo;

    public SyncHelper(SlotDraftItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    @Transactional
    public int sync(SlotSyncAction action, SlotItemType itemType, PublicationKind kind, long itemId) {

        if (itemId <= 0) {
            throw Status.INVALID_ARGUMENT.withDescription("itemId must be positive.").asRuntimeException();
        }
        if (action == SlotSyncAction.SLOT_SYNC_ACTION_UNSPECIFIED) {
            throw Status.INVALID_ARGUMENT.withDescription("action is required.").asRuntimeException();
        }

        return switch (action) {
            case SLOT_SYNC_ACTION_DELETED -> handleDeleted(kind, itemId);
            case SLOT_SYNC_ACTION_UPDATED -> handleUpdated(kind, itemType, itemId);
            default -> 0;
        };
    }

    private int handleDeleted(PublicationKind kind, long itemId) {
        return itemRepo.deleteAllByKindAndPublicationId(kind, itemId);
    }

//    private int handleUpdated(PublicationKind kind, SlotItemType itemType, long itemId) {
//
//
//        List<SlotDraftItem> items = itemRepo.findAllByKindAndPublicationIdFetchDraft(kind, itemId);
//        if (items.isEmpty()) return 0;
//
//        int updated = 0;
//        int removedForLimits = 0;
//
//        for (SlotDraftItem di : items) {
//            SlotDraft draft = di.getDraft();
//            if (draft == null) {
//                itemRepo.delete(di);
//                continue;
//            }
//
//            final ItemForSlots item;
//            try {
//                item = ArticleSlotsClient.getItemForSlots(draft.getUserId(), itemType, itemId);
//            } catch (StatusRuntimeException e) {
//
//                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
//                    itemRepo.delete(di);
//                }
//                continue;
//            }
//
//
//            if (item.getPublicationYear() != draft.getEvalYear()) {
//                itemRepo.delete(di);
//                continue;
//            }
//
//
//            final var ownerSt = ProfilesClient
//                    .getOrCreateStatement(draft.getUserId(), draft.getDisciplineId(), draft.getEvalYear())
//                    .getStatement();
//
//            SlotComputation comp = SlotComute.computeSlotValueAndPointsRecalc(
//                    draft.getUserId(),
//                    draft.getDisciplineId(),
//                    draft.getEvalYear(),
//                    kind,
//                    item,
//                    ownerSt,
//                    k
//            );
//
//            BigDecimal oldSlot = SlotComute.nz(di.getSlotValue());
//            BigDecimal newSlot = SlotComute.nz(comp.slotValue());
//
//            BigDecimal usedNow = SlotComute.nz(itemRepo.sumSlotValue(draft.getId()));
//            BigDecimal usedAfter = usedNow.subtract(oldSlot).add(newSlot);
//
//            if (usedAfter.compareTo(SlotComute.nz(draft.getMaxSlots())) > 0) {
//                itemRepo.delete(di);
//                removedForLimits++;
//
//                continue;
//            }
//
//            if (isMono(kind)) {
//                BigDecimal usedMonoNow = SlotComute.nz(itemRepo.sumSlotValueMono(draft.getId()));
//                BigDecimal oldMono = oldSlot;
//                BigDecimal newMono = newSlot;
//
//                BigDecimal usedMonoAfter = usedMonoNow.subtract(oldMono).add(newMono);
//
//                if (usedMonoAfter.compareTo(SlotComute.nz(draft.getMaxMonoSlots())) > 0) {
//                    itemRepo.delete(di);
//                    removedForLimits++;
//                    continue;
//                }
//            }
//
//            di.setTitle(item.getTitle() == null ? "" : item.getTitle());
//            di.setPublicationYear(item.getPublicationYear());
//            di.setPoints(comp.points());
//            di.setSlotValue(comp.slotValue());
//            di.setPointsRecalc(comp.pointsRecalc());
//
//            itemRepo.save(di);
//            updated++;
//        }
//
//        return updated;
//    }
private int handleUpdated(PublicationKind kind, SlotItemType itemType, long itemId) {

    List<SlotDraftItem> items = itemRepo.findAllByKindAndPublicationIdFetchDraft(kind, itemId);
    if (items.isEmpty()) return 0;

    int updated = 0;
    int removedForLimits = 0;

    // cache dla sprawdzania "ewaluowany w dyscyplinie" (per sync call)
    Map<String, Boolean> evalCache = new HashMap<>();

    for (SlotDraftItem di : items) {
        SlotDraft draft = di.getDraft();
        if (draft == null) {
            itemRepo.delete(di);
            continue;
        }

        final ItemForSlots item;
        try {
            item = ArticleSlotsClient.getItemForSlots(draft.getUserId(), itemType, itemId);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                itemRepo.delete(di);
            }
            continue;
        }

        if (item.getPublicationYear() != draft.getEvalYear()) {
            itemRepo.delete(di);
            continue;
        }

        final var ownerSt = ProfilesClient
                .getOrCreateStatement(draft.getUserId(), draft.getDisciplineId(), draft.getEvalYear())
                .getStatement();

        // >>> KLUCZOWE: policz k (autorzy ewaluowani w tej dyscyplinie i roku)
        int k = computeK(item, draft.getDisciplineId(), draft.getEvalYear(), evalCache);

        SlotComputation comp = SlotComute.computeSlotValueAndPointsRecalc(
                draft.getUserId(),
                draft.getDisciplineId(),
                draft.getEvalYear(),
                kind,
                item,
                ownerSt,
                k
        );

        BigDecimal oldSlot = SlotComute.nz(di.getSlotValue());
        BigDecimal newSlot = SlotComute.nz(comp.slotValue());

        BigDecimal usedNow = SlotComute.nz(itemRepo.sumSlotValue(draft.getId()));
        BigDecimal usedAfter = usedNow.subtract(oldSlot).add(newSlot);

        if (usedAfter.compareTo(SlotComute.nz(draft.getMaxSlots())) > 0) {
            itemRepo.delete(di);
            removedForLimits++;
            continue;
        }

        if (isMono(kind)) {
            BigDecimal usedMonoNow = SlotComute.nz(itemRepo.sumSlotValueMono(draft.getId()));
            BigDecimal usedMonoAfter = usedMonoNow.subtract(oldSlot).add(newSlot);

            if (usedMonoAfter.compareTo(SlotComute.nz(draft.getMaxMonoSlots())) > 0) {
                itemRepo.delete(di);
                removedForLimits++;
                continue;
            }
        }

        di.setTitle(item.getTitle() == null ? "" : item.getTitle());
        di.setPublicationYear(item.getPublicationYear());
        di.setPoints(comp.points());
        di.setSlotValue(comp.slotValue());
        di.setPointsRecalc(comp.pointsRecalc());

        itemRepo.save(di);
        updated++;
    }

    return updated;
}


    private static String kCacheKey(long authorId, long disciplineId, int evalYear) {
        return authorId + ":" + disciplineId + ":" + evalYear;
    }

    private boolean isEvaluatedInDisciplineCached(long authorId, long disciplineId, int evalYear, Map<String, Boolean> cache) {
        if (authorId <= 0) return false;
        String key = kCacheKey(authorId, disciplineId, evalYear);
        Boolean v = cache.get(key);
        if (v != null) return v;

        boolean ok;
        try {
            // U Ciebie: rzuca wyjątek, jeśli autor NIE jest przypisany do dyscypliny.
            ProfilesClient.getOrCreateStatement(authorId, disciplineId, evalYear);
            ok = true;
        } catch (StatusRuntimeException e) {
            ok = false;
        } catch (Exception e) {
            ok = false;
        }

        cache.put(key, ok);
        return ok;
    }

    private int computeK(ItemForSlots item, long disciplineId, int evalYear, Map<String, Boolean> cache) {
        int k = 0;
        for (SlotAuthor a : item.getAuthorsList()) {
            long aid = a.getUserId();
            if (aid <= 0) continue; // external
            if (isEvaluatedInDisciplineCached(aid, disciplineId, evalYear, cache)) {
                k++;
            }
        }
        return Math.max(1, k);
    }
}
