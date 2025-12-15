package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class ListMeinMonoVersionsRequest {
    private int page;
    private int size;
    private String sortDir;
}
