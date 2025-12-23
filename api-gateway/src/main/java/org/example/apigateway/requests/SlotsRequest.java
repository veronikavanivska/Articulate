package org.example.apigateway.requests;

import lombok.Data;

@Data
public class SlotsRequest {
    Long disciplineId;
    SlotItemType itemType;
    Long itemId;
}
