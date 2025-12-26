package org.example.apigateway.requests;

import lombok.Data;

@Data
public class ListProfilesRequest {
    private String fullName;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDir;
}
