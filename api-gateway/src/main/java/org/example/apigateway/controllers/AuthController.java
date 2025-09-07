package org.example.apigateway.controllers;

import org.example.apigateway.clients.AuthClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.auth.*;
import org.example.apigateway.responses.Response;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {

    @PostMapping("/registration")
    public Response<Void> registration(@RequestBody RegistrationRequest registrationRequest) {
        var apiResponse = AuthClient.register(registrationRequest.getEmail(), registrationRequest.getPassword());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest loginRequest) {
        var apiResponse = AuthClient.login(loginRequest.getEmail(), loginRequest.getPassword());
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
        var apiResponse = AuthClient.refresh(refreshRequest.getRefreshToken());

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

        var apiResponse = AuthClient.changePassword(userId, changePasswordRequest.getPassword(), changePasswordRequest.getNewPassword());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );

    }

    @PostMapping("/change-email")
    public Response<Void> changeEmail(@RequestBody ChangeEmailRequest changeEmailRequest) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var apiResponse = AuthClient.changeEmail(userId, changeEmailRequest.getEmail());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }


    @DeleteMapping("/delete-me")
    public Response<Void> delete() {
            Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
            var apiResponse = AuthClient.deleteUser(userId);

           return new Response<>(
                    apiResponse.getCode(),
                    apiResponse.getMessage()
           );
    }


    @PostMapping("/admin/assign-role")
    public Response<Void> assignRole(@RequestBody AssignRoleRequest assignRoleRequest) {
        var apiResponse = AuthClient.assignRole(assignRoleRequest.getUserId(), assignRoleRequest.getRoleName());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/admin/revoke-role")
    public Response<Void> revokeRole(@RequestBody RevokeRoleRequest revokeRoleRequest) {
        var apiResponse = AuthClient.revokeRole(revokeRoleRequest.getUserId(), revokeRoleRequest.getRoleName());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/admin/enable-disable")
    public Response<Void> enableDisableUser(@RequestBody EnableDisableUserRequest enableDisableUserRequest) {
        var apiResponse = AuthClient.enableDisableUser(enableDisableUserRequest.getUserId());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/logout")
    public Response<Void> logout() {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var apiResponse = AuthClient.logOut(userId);

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }
}
