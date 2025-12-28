package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class ListMeinMonoPublishersRequest {
    private Long versionId;
    private Integer page;
    private Integer size;
    private String sortDir;
    private String title;

}
