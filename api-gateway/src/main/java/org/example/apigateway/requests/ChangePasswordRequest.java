package org.example.apigateway.requests;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    String password;
    String newPassword;

}
