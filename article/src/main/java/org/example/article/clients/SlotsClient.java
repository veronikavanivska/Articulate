package org.example.article.clients;

import com.example.generated.*;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.article.Client;
import org.springframework.stereotype.Component;

@Client(host = "${slot.server.host}", port = "${slot.server.port}")
@Component
public class SlotsClient {

    private SlotServiceGrpc.SlotServiceBlockingStub stub;

    public void notifyUpdated(SlotItemType type, long id) {
        stub.syncSlotItem(SyncSlotItemRequest.newBuilder()
                .setAction(SlotSyncAction.SLOT_SYNC_ACTION_UPDATED)
                .setItemType(type)
                .setItemId(id)
                .build());
    }

    public void notifyDeleted(SlotItemType type, long id) {
        stub.syncSlotItem(SyncSlotItemRequest.newBuilder()
                .setAction(SlotSyncAction.SLOT_SYNC_ACTION_DELETED)
                .setItemType(type)
                .setItemId(id)
                .build());
    }

    public void init(Channel channel) {
        stub = SlotServiceGrpc.newBlockingStub(channel);
    }

}
