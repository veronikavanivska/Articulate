package org.example.apigateway.requests.auth;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    String password;
    String newPassword;

}
