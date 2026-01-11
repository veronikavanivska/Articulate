package org.example.slots.clients;

import com.example.generated.GetOrCreateStatementRequest;
import com.example.generated.GetOrCreateStatementResponse;
import com.example.generated.ProfilesServiceGrpc;
import io.grpc.Channel;
import org.example.slots.Client;
import org.springframework.stereotype.Component;

@Client(host = "${profiles.server.host}", port = "${profiles.server.port}")
@Component
public class ProfilesClient {

    private ProfilesServiceGrpc.ProfilesServiceBlockingStub stub;

    public GetOrCreateStatementResponse getOrCreateStatement(long userId, long disciplineId, int evalYear) {
        return stub.getOrCreateStatement(
                GetOrCreateStatementRequest.newBuilder()
                        .setUserId(userId)
                        .setDisciplineId(disciplineId)
                        .setEvalYear(evalYear)
                        .build()
        );
    }

    public void init(Channel channel) {
        stub = ProfilesServiceGrpc.newBlockingStub(channel);
    }
}
