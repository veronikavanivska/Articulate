package org.example.auth.clients;

import com.example.generated.*;
import org.example.auth.Client;
import io.grpc.Channel;

@Client(
        host = "${profiles.server.host}", port = "${profiles.server.port}"
)
public class ProfileCommandClient {

    private static  ProfilesCommandsServiceGrpc.ProfilesCommandsServiceBlockingStub stub;

    public static ApiResponse ensureUserProfile(long userId) {
        return stub.ensureUserProfile(
                EnsureUserProfileRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public static ApiResponse ensureWorkerProfile(long userId) {
        return stub.ensureWorkerProfile(
                EnsureWorkerProfileRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public static ApiResponse ensureAdminProfile(long userId) {
        return stub.ensureAdminProfile(
                EnsureAdminProfileRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public static ApiResponse deleteWorkerProfile(long userId) {

        return stub.deleteWorkerProfile(
                UserId.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public static ApiResponse deleteAdminProfile(long userId) {

        return stub.deleteAdminProfile(
                UserId.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public static ApiResponse deleteAllProfiles(long userId) {

        return stub.deleteAllProfiles(
                UserId.newBuilder()
                        .setUserId(userId)
                        .build()
        );
    }

    public static void init(Channel channel) {
        stub = ProfilesCommandsServiceGrpc.newBlockingStub(channel);
    }
}
