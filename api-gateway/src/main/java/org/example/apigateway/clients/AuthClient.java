package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.example.apigateway.mappers.RoleMapper;
import org.springframework.stereotype.Component;


@Client(host = "${auth.server.host}",
        port = "${auth.server.port}"
)
@Component
public class AuthClient {

    private AuthServiceGrpc.AuthServiceBlockingStub stub;

    public ApiResponse register(String email, String password) {
        RegistrationRequest req = RegistrationRequest.newBuilder().
                setEmail(email).
                setPassword(password)
                .build();

        return stub.register(req);
    }


    public LoginResponse login(String email, String password) {
        LoginRequest req = LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();

        return stub.login(req);
    }

    public RefreshResponse refresh(String rawToken) {
        RefreshRequest req = RefreshRequest.newBuilder()
                .setRefreshToken(rawToken)
                .build();

        return stub.refresh(req);
    }

    public ApiResponse changePassword(Long userId, String password, String newPassword) {
        ChangePasswordRequest req = ChangePasswordRequest.newBuilder()
                .setUserId(userId)
                .setPassword(password)
                .setNewPassword(newPassword)
                .build();

        return stub.changePassword(req);
    }

    public ApiResponse changeEmail(Long userId, String newEmail) {
        ChangeEmailRequest req = ChangeEmailRequest.newBuilder()
                .setUserId(userId)
                .setNewEmail(newEmail)
                .build();

        return stub.changeEmail(req);
    }

    public ApiResponse deleteUser(Long userId) {
        DeleteRequest req = DeleteRequest.newBuilder()
                .setUserId(userId)
                .build();

        return stub.deleteUser(req);
    }

    public ApiResponse assignRole(Long userId, org.example.apigateway.requests.auth.RoleName roleName) {
        AssignRoleRequest req = AssignRoleRequest.newBuilder()
                .setUserId(userId)
                .setRoleName(RoleMapper.map(roleName))
                .build();

        return stub.assignRole(req);
    }

    public ApiResponse revokeRole(Long userId, org.example.apigateway.requests.auth.RoleName roleName) {
        RevokeRoleRequest req = RevokeRoleRequest.newBuilder()
                .setUserId(userId)
                .setRoleName(RoleMapper.map(roleName))
                .build();

        return stub.revokeRole(req);
    }

    public ApiResponse enableDisableUser(Long userId) {
        DisableUserRequest req = DisableUserRequest.newBuilder()
                .setUserId(userId)
                .build();

        return stub.enableDisableUser(req);
    }

    public ApiResponse logOut(Long userId) {
        LogoutRequest req = LogoutRequest.newBuilder()
                .setUserId(userId)
                .build();
        return stub.logout(req);
    }

    public void init(Channel channel) {
        stub = AuthServiceGrpc.newBlockingStub(channel);
    }

}
