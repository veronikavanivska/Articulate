package org.example.slots.clients;

import com.example.generated.ArticleServiceGrpc;
import com.example.generated.CycleItem;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.slots.Client;
import org.springframework.stereotype.Component;

@Client(host = "${article.server.host}", port = "${article.server.port}")
@Component
public class ArticleClient {

    private ArticleServiceGrpc.ArticleServiceBlockingStub stub;

    public CycleItem getActiveEvalCycle() {
        return stub.getActiveEvalCycle(Empty.getDefaultInstance());
    }

    public void init(Channel channel) {
        stub = ArticleServiceGrpc.newBlockingStub(channel);
    }
}