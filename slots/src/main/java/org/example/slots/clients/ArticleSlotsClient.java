package org.example.slots.clients;

import com.example.generated.ArticleSlotsServiceGrpc;
import com.example.generated.GetItemForSlotsRequest;
import com.example.generated.ItemForSlots;
import com.example.generated.SlotItemType;
import io.grpc.Channel;
import org.example.slots.Client;
import org.springframework.stereotype.Component;

@Client(host = "${article.server.host}", port = "${article.server.port}")
@Component
public class ArticleSlotsClient {

    private ArticleSlotsServiceGrpc.ArticleSlotsServiceBlockingStub stub;

    public ItemForSlots getItemForSlots(long userId, SlotItemType type, long id) {
        return stub.getItemForSlots(
                GetItemForSlotsRequest.newBuilder()
                        .setUserId(userId)
                        .setItemType(type)
                        .setItemId(id)
                        .build()
        );
    }

    public void init(Channel channel) {
        stub = ArticleSlotsServiceGrpc.newBlockingStub(channel);
    }
}
