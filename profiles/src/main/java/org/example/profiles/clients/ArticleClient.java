package org.example.profiles.clients;

import com.example.generated.ArticleProfileSyncServiceGrpc;
import com.example.generated.SyncAuthorFullNameRequest;
import com.example.generated.SyncAuthorFullNameResponse;
import org.example.profiles.Client;
import io.grpc.Channel;
import org.springframework.stereotype.Component;

@Client(host = "${article.server.host}", port = "${article.server.port}")
@Component
public class ArticleClient {

    private ArticleProfileSyncServiceGrpc.ArticleProfileSyncServiceBlockingStub stub;

    public SyncAuthorFullNameResponse syncAuthorFullName(long userId, String fullName) {
        if (stub == null) {
            throw new IllegalStateException("ArticleClient is not initialized. Did you forget to call init(channel)?");
        }

        String name = fullName == null ? "" : fullName.trim();

        SyncAuthorFullNameRequest req = SyncAuthorFullNameRequest.newBuilder()
                .setUserId(userId)
                .setFullName(name)
                .build();

        return stub.syncAuthorFullName(req);
    }

    public void init(Channel channel) {
        stub = ArticleProfileSyncServiceGrpc.newBlockingStub(channel);
    }
}