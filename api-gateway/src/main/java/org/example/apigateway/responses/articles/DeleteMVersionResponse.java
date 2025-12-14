package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class DeleteMVersionResponse {
    private long jobId;
    private String message;
}
