package org.example.auth.clients;

import com.example.generated.*;
import com.rabbitmq.client.Command;
import org.example.auth.Client;
import io.grpc.Channel;
import org.springframework.stereotype.Component;

@Client(
        host = "${profiles.server.host}", port = "${profiles.server.port}"
)
@Component
public class ProfileCommandClient {

    private  ProfilesCommandsServiceGrpc.ProfilesCommandsServiceBlockingStub stub;

    public ApiResponse ensureUserProfile(long userId) {
        return stub.ensureUserProfile(
                EnsureUserProfileRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse ensureWorkerProfile(long userId) {
        return stub.ensureWorkerProfile(
                EnsureWorkerProfileRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse ensureAdminProfile(long userId) {
        return stub.ensureAdminProfile(
                EnsureAdminProfileRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse deleteWorkerProfile(long userId) {

        return stub.deleteWorkerProfile(
                UserId.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse deleteAdminProfile(long userId) {

        return stub.deleteAdminProfile(
                UserId.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse deleteAllProfiles(long userId) {

        return stub.deleteAllProfiles(
                UserId.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public void init(Channel channel) {
        stub = ProfilesCommandsServiceGrpc.newBlockingStub(channel);
    }
}
