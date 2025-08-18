package org.example.apigateway.requests;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class RefreshRequest {
    private String refreshToken;
}
