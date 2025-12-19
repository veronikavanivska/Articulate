package org.example.slots.clients;

import com.example.generated.ArticleServiceGrpc;
import com.example.generated.CycleItem;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.slots.Client;

@Client(host = "${article.server.host}", port = "${article.server.port}")
public class ArticleClient {

    private static ArticleServiceGrpc.ArticleServiceBlockingStub stub;

    public static CycleItem getActiveEvalCycle() {
        return stub.getActiveEvalCycle(Empty.getDefaultInstance());
    }

    public static void init(Channel channel) {
        stub = ArticleServiceGrpc.newBlockingStub(channel);
    }
}