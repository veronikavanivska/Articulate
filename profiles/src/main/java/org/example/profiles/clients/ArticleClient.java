package org.example.profiles.clients;

import com.example.generated.ArticleProfileSyncServiceGrpc;
import com.example.generated.SyncAuthorFullNameRequest;
import com.example.generated.SyncAuthorFullNameResponse;
import org.example.profiles.Client;
import io.grpc.Channel;

@Client(host = "${article.server.host}", port = "${article.server.port}")
public class ArticleClient {

    private static ArticleProfileSyncServiceGrpc.ArticleProfileSyncServiceBlockingStub stub;

    public static SyncAuthorFullNameResponse syncAuthorFullName(long userId, String fullName) {
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

    public static void init(Channel channel) {
        stub = ArticleProfileSyncServiceGrpc.newBlockingStub(channel);
    }
}