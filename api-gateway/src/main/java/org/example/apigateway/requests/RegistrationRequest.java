package org.example.apigateway.requests;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String email;
    private String password;
}
