package org.example.apigateway.responses;

import lombok.Data;

@Data
public class ApiResponse {
    private int code;
    private String message;
}
