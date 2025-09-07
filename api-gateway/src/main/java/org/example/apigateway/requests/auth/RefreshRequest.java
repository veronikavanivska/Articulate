package org.example.apigateway.requests.auth;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
