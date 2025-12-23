package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.example.apigateway.mappers.SlotItemTypeMapper;
import org.example.apigateway.requests.SlotItemType;

@Client(host = "${slots.server.host}",
        port = "${slots.server.port}"
)
public class SlotsClient {

    private static SlotServiceGrpc.SlotServiceBlockingStub stub;


    public static DraftView addToActiveSlot(Long userId, Long disciplineId, SlotItemType itemType, Long itemId) {
        AddToSlotRequest req = AddToSlotRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setItemType(SlotItemTypeMapper.map(itemType))
                .setItemId(itemId)
                .build();
        return stub.addToActiveSlot(req);
    }

    public static DraftView removeFromActiveSlot(Long userId, Long disciplineId, SlotItemType itemType, Long itemId) {
        RemoveFromSlotRequest req = RemoveFromSlotRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setItemType(SlotItemTypeMapper.map(itemType))
                .setItemId(itemId)
                .build();
        return stub.removeFromActiveSlot(req);
    }

    public static DraftView getDraft(Long userId, Long disciplineId, Long cycleId, Integer evalYear) {
        GetDraftRequest req = GetDraftRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setCycleId(cycleId != null ? cycleId : 0L)
                .setEvalYear(evalYear != null ? evalYear : 0)
                .build();
        return stub.getDraft(req);
    }



    public static void init(Channel channel) {
        stub = SlotServiceGrpc.newBlockingStub(channel);
    }
}
