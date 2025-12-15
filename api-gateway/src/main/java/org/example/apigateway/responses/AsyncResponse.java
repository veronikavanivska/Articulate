package org.example.apigateway.responses;

import lombok.Data;

@Data
public class AsyncResponse {
    private long jobId;
    private String message;
}
