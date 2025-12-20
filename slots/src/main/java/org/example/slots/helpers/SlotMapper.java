package org.example.slots.helpers;

import com.example.generated.CycleItem;
import com.example.generated.DraftItem;
import com.example.generated.DraftView;
import com.example.generated.SlotItemType;
import org.example.slots.entities.PublicationKind;
import org.example.slots.entities.SlotDraft;
import org.example.slots.entities.SlotDraftItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SlotMapper {


    public static DraftView toEmptyDraftView(long userId,
                                      long disciplineId,
                                      long cycleId,
                                      int evalYear,
                                      boolean editable,
                                      double maxSlots) {
        return DraftView.newBuilder()
                .setDraftId(0)
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setCycleId(cycleId)
                .setEvalYear(evalYear)
                .setEditable(editable)
                .setMaxSlots(maxSlots)
                .setUsedSlots(0)
                .setFreeSlots(maxSlots)
                .setSumPoints(0)
                .setSumPointsRecalc(0)
                .build();
    }

    public static DraftView toDraftView(SlotDraft draft,
                                 CycleItem active,
                                 List<SlotDraftItem> items,
                                 BigDecimal usedSlots,
                                 BigDecimal sumPoints,
                                 BigDecimal sumPointsRecalc) {

        boolean editable = active.getIsActive()
                && active.getId() == draft.getEvalCycleId()
                && active.getActiveYear() == draft.getEvalYear();

        BigDecimal maxSlots = nz(draft.getMaxSlots());
        BigDecimal free = maxSlots.subtract(nz(usedSlots));
        if (free.signum() < 0) free = BigDecimal.ZERO;

        DraftView.Builder b = DraftView.newBuilder()
                .setDraftId(draft.getId())
                .setUserId(draft.getUserId())
                .setDisciplineId(draft.getDisciplineId())
                .setCycleId(draft.getEvalCycleId())
                .setEvalYear(draft.getEvalYear())
                .setEditable(editable)
                .setMaxSlots(maxSlots.doubleValue())
                .setUsedSlots(nz(usedSlots).doubleValue())
                .setFreeSlots(free.doubleValue())
                .setSumPoints(nz(sumPoints).doubleValue())
                .setSumPointsRecalc(nz(sumPointsRecalc).doubleValue());

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

    public static PublicationKind toKind(SlotItemType t) {
        return switch (t) {
            case SLOT_ITEM_ARTICLE -> PublicationKind.ARTICLE;
            case SLOT_ITEM_MONOGRAPH -> PublicationKind.MONOGRAPH;
            case SLOT_ITEM_CHAPTER -> PublicationKind.CHAPTER;
            default -> throw new IllegalArgumentException("Unsupported itemType: " + t);
        };
    }

    private static SlotItemType fromKind(PublicationKind k) {
        return switch (k) {
            case ARTICLE -> SlotItemType.SLOT_ITEM_ARTICLE;
            case MONOGRAPH -> SlotItemType.SLOT_ITEM_MONOGRAPH;
            case CHAPTER -> SlotItemType.SLOT_ITEM_CHAPTER;
        };
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
