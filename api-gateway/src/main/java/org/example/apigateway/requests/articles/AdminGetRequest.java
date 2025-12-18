package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class AdminGetRequest {
    private long id;
    private long ownerId;
}
