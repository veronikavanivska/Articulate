package org.example.slots.clients;

import com.example.generated.ArticleSlotsServiceGrpc;
import com.example.generated.GetItemForSlotsRequest;
import com.example.generated.ItemForSlots;
import com.example.generated.SlotItemType;
import io.grpc.Channel;
import org.example.slots.Client;

@Client(host = "${article.server.host}", port = "${article.server.port}")
public class ArticleSlotsClient {

    private static ArticleSlotsServiceGrpc.ArticleSlotsServiceBlockingStub stub;

    public static ItemForSlots getItemForSlots(long userId, SlotItemType type, long id) {
        return stub.getItemForSlots(
                GetItemForSlotsRequest.newBuilder()
                        .setUserId(userId)
                        .setItemType(type)
                        .setItemId(id)
                        .build()
        );
    }

    public static void init(Channel channel) {
        stub = ArticleSlotsServiceGrpc.newBlockingStub(channel);
    }
}
