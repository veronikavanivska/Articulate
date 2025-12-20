package org.example.article.clients;

import com.example.generated.*;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.article.Client;

@Client(host = "${slot.server.host}", port = "${slot.server.port}")
public class SlotsClient {


    private static SlotServiceGrpc.SlotServiceBlockingStub stub;

    public static void notifyUpdated(SlotItemType type, long id) {
        stub.syncSlotItem(SyncSlotItemRequest.newBuilder()
                .setAction(SlotSyncAction.SLOT_SYNC_ACTION_UPDATED)
                .setItemType(type)
                .setItemId(id)
                .build());
    }

    public static void notifyDeleted(SlotItemType type, long id) {
        stub.syncSlotItem(SyncSlotItemRequest.newBuilder()
                .setAction(SlotSyncAction.SLOT_SYNC_ACTION_DELETED)
                .setItemType(type)
                .setItemId(id)
                .build());
    }

    public static void init(Channel channel) {
        stub = SlotServiceGrpc.newBlockingStub(channel);
    }

}
