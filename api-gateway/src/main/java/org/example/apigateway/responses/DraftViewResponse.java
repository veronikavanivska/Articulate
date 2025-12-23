package org.example.apigateway.responses;

import lombok.Data;

import java.util.List;

@Data
public class DraftViewResponse {
    long draftId;
    long userId;
    long disciplineId;
    long cycleId;

    int evalYear;

    boolean editable;
    double maxSlots ;
    double usedSlots;
    double freeSlots ;

    double sumPoints ;
    double sumPointsRecalc;

   List<DraftItemResponse> items;
}
