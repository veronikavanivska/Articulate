package org.example.slots.helpers;

import com.example.generated.ItemForSlots;
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

import java.util.List;

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

    private int handleUpdated(PublicationKind kind, SlotItemType itemType, long itemId) {


        List<SlotDraftItem> items = itemRepo.findAllByKindAndPublicationIdFetchDraft(kind, itemId);
        if (items.isEmpty()) return 0;

        int updated = 0;

        for (SlotDraftItem di : items) {
            SlotDraft draft = di.getDraft();


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

            SlotComputation comp = SlotComute.computeSlotValueAndPointsRecalc(
                    draft.getUserId(),
                    draft.getDisciplineId(),
                    draft.getEvalYear(),
                    kind,
                    item,
                    ownerSt
            );

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
}
