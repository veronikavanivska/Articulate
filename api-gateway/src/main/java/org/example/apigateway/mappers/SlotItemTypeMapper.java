package org.example.apigateway.mappers;

import org.example.apigateway.requests.SlotItemType;
import org.springframework.stereotype.Component;

import static com.example.generated.SlotItemType.*;

@Component
public class SlotItemTypeMapper {

    public static SlotItemType map(com.example.generated.SlotItemType slotItemType) {
        if(slotItemType == null || slotItemType == com.example.generated.SlotItemType.UNRECOGNIZED) {
            throw new IllegalArgumentException("SlotType cannot be null");
        }
        switch (slotItemType) {
            case SLOT_ITEM_ARTICLE -> {
                return SlotItemType.SLOT_ITEM_ARTICLE;
            }
            case SLOT_ITEM_MONOGRAPH -> {
                return SlotItemType.SLOT_ITEM_MONOGRAPH;
            }
            case SLOT_ITEM_CHAPTER ->{
                return SlotItemType.SLOT_ITEM_CHAPTER;
            }
            default -> throw new IllegalArgumentException("Unknown SlotType: " + slotItemType);
        }

    }

    public static com.example.generated.SlotItemType map(SlotItemType slotItemType) {
        if(slotItemType == null) {
            throw new IllegalArgumentException("SlotType cannot be null");
        }
        switch (slotItemType) {
            case SLOT_ITEM_ARTICLE -> {
                return SLOT_ITEM_ARTICLE;
            }
            case SLOT_ITEM_MONOGRAPH -> {
                return SLOT_ITEM_MONOGRAPH;
            }
            case SLOT_ITEM_CHAPTER ->{
                return SLOT_ITEM_CHAPTER;
            }
            default -> throw new IllegalArgumentException("Unknown SlotType: " + slotItemType);
        }

    }
}
