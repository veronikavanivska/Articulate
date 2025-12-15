package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class AdminGetPublicationRequest {
    private long id;
    private long ownerId;
}
