package org.example.slots.clients;

import com.example.generated.GetOrCreateStatementRequest;
import com.example.generated.GetOrCreateStatementResponse;
import com.example.generated.ProfilesServiceGrpc;
import io.grpc.Channel;
import org.example.slots.Client;

@Client(host = "${profiles.server.host}", port = "${profiles.server.port}")
public class ProfilesClient {

    private static ProfilesServiceGrpc.ProfilesServiceBlockingStub stub;

    public static GetOrCreateStatementResponse getOrCreateStatement(long userId, long disciplineId, int evalYear) {
        return stub.getOrCreateStatement(
                GetOrCreateStatementRequest.newBuilder()
                        .setUserId(userId)
                        .setDisciplineId(disciplineId)
                        .setEvalYear(evalYear)
                        .build()
        );
    }

    public static void init(Channel channel) {
        stub = ProfilesServiceGrpc.newBlockingStub(channel);
    }
}
