package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class JobStatusResponse {
    private long jobId;
    private String type;
    private String status;
    private String error;
}
