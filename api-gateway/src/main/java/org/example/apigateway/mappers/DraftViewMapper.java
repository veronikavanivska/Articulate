package org.example.apigateway.mappers;

import com.example.generated.DraftItem;
import com.example.generated.DraftView;
import org.example.apigateway.requests.SlotItemType;
import org.example.apigateway.responses.DraftItemResponse;
import org.example.apigateway.responses.DraftViewResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DraftViewMapper {


    public static DraftViewResponse toResponse(DraftView draftView) {
        DraftViewResponse draftViewResponse = new DraftViewResponse();
        draftViewResponse.setDraftId(draftView.getDraftId());
        draftViewResponse.setUserId(draftView.getUserId());
        draftViewResponse.setDisciplineId(draftView.getDisciplineId());
        draftViewResponse.setCycleId(draftView.getCycleId());
        draftViewResponse.setEvalYear(draftView.getEvalYear());
        draftViewResponse.setEditable(draftView.getEditable());
        draftViewResponse.setMaxSlots(draftView.getMaxSlots());
        draftViewResponse.setUsedSlots(draftView.getUsedSlots());
        draftViewResponse.setFreeSlots(draftView.getFreeSlots());
        draftViewResponse.setSumPoints(draftView.getSumPoints());
        draftViewResponse.setSumPointsRecalc(draftView.getSumPointsRecalc());

        List<DraftItemResponse> draftItemResponses = new ArrayList<>();
        for(DraftItem draftItem : draftView.getItemsList()){
            DraftItemResponse draftItemResponse = new DraftItemResponse();
            draftItemResponse.setItemId(draftItem.getItemId());
            draftItemResponse.setItemType(SlotItemTypeMapper.map(draftItem.getItemType()));
            draftItemResponse.setPublicationYear(draftItem.getPublicationYear());
            draftItemResponse.setTitle(draftItem.getTitle());

            draftItemResponse.setPoints(draftItem.getPoints());
            draftItemResponse.setSlotValue(draftItem.getSlotValue());
            draftItemResponse.setPointsRecalc(draftItem.getPointsRecalc());

            draftItemResponses.add(draftItemResponse);
        }

        draftViewResponse.setItems(draftItemResponses);
        return draftViewResponse;
    }
}
