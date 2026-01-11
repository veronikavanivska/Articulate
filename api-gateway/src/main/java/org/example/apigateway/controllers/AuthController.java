package org.example.apigateway.controllers;

import org.example.apigateway.clients.AuthClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.auth.*;
import org.example.apigateway.responses.Response;
import org.example.apigateway.responses.RoleResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthClient authClient;

    public AuthController(AuthClient authClient) {
        this.authClient = authClient;
    }

    @PostMapping("/registration")
    public Response<Void> registration(@RequestBody RegistrationRequest registrationRequest) {
        var apiResponse = authClient.register(registrationRequest.getEmail(), registrationRequest.getPassword());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest loginRequest) {
        var apiResponse = authClient.login(loginRequest.getEmail(), loginRequest.getPassword());
        var api = apiResponse.getApiResponse();
        return new Response<>(
                api.getCode(),
                api.getMessage(),
                apiResponse.getAccessToken(),
                apiResponse.getRefreshToken()
        );
    }

    @PostMapping("/refresh")
    public Response<String> refresh(@RequestBody RefreshRequest refreshRequest) {
        var apiResponse = authClient.refresh(refreshRequest.getRefreshToken());

        var api = apiResponse.getResponse();
        return new Response<>(
                api.getCode(),
                api.getMessage(),
                apiResponse.getAccessToken(),
                apiResponse.getRefreshToken()
        );
    }


    @PostMapping("/change-password")
    public Response<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var apiResponse = authClient.changePassword(userId, changePasswordRequest.getPassword(), changePasswordRequest.getNewPassword());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );

    }

    @PostMapping("/change-email")
    public Response<Void> changeEmail(@RequestBody ChangeEmailRequest changeEmailRequest) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var apiResponse = authClient.changeEmail(userId, changeEmailRequest.getEmail());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }


    @DeleteMapping("/delete-me")
    public Response<Void> delete() {
            Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
            var apiResponse = authClient.deleteUser(userId);

           return new Response<>(
                    apiResponse.getCode(),
                    apiResponse.getMessage()
           );
    }


    @PostMapping("/admin/assign-role")
    public Response<Void> assignRole(@RequestBody AssignRoleRequest assignRoleRequest) {
        var apiResponse = authClient.assignRole(assignRoleRequest.getUserId(), assignRoleRequest.getRoleName());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/admin/revoke-role")
    public Response<Void> revokeRole(@RequestBody RevokeRoleRequest revokeRoleRequest) {
        var apiResponse = authClient.revokeRole(revokeRoleRequest.getUserId(), revokeRoleRequest.getRoleName());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/admin/enable-disable")
    public Response<Void> enableDisableUser(@RequestBody EnableDisableUserRequest enableDisableUserRequest) {
        var apiResponse = authClient.enableDisableUser(enableDisableUserRequest.getUserId());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/logout")
    public Response<Void> logout() {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var apiResponse = authClient.logOut(userId);

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @GetMapping("/roles")
    public List<RoleResponse> roles() {
        List<RoleResponse> out = new ArrayList<>();

        for (RoleName r : RoleName.values()) {
            RoleResponse opt = new RoleResponse();
            opt.setValue(r.name());
            opt.setLabel(mapRoleLabel(r));
            out.add(opt);
        }
        return out;
    }

    private String mapRoleLabel(RoleName r) {
        return switch (r) {
            case ROLE_ADMIN -> "ADMIN";
            case ROLE_WORKER -> "WORKER";
            case ROLE_USER -> "USER";
            default -> r.name();
        };
    }
}
