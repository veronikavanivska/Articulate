package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;


@Client(host = "${auth.server.host}",
        port = "${auth.server.port}"
)
public class AuthClient {

    private static AuthServiceGrpc.AuthServiceBlockingStub stub;

    public static ApiResponse register(String email, String password){
        RegistrationRequest req = RegistrationRequest.newBuilder().
                setEmail(email).
                setPassword(password)
                .build();

        return stub.register(req);
    }
    public static void init(Channel channel) {
        stub = AuthServiceGrpc.newBlockingStub(channel);
    }

    public static LoginResponse login(String email, String password){
        LoginRequest req = LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();

        return stub.login(req);
    }

    public static RefreshResponse refresh(String rawToken ){
        RefreshRequest req = RefreshRequest.newBuilder()
                .setRefreshToken(rawToken)
                .build();

        return stub.refresh(req);
    }

    public static ApiResponse changePassword(Long userId, String password, String newPassword){
        ChangePasswordRequest req = ChangePasswordRequest.newBuilder()
                .setUserId(userId)
                .setPassword(password)
                .setNewPassword(newPassword)
                .build();

        return stub.changePassword(req);
    }
}
