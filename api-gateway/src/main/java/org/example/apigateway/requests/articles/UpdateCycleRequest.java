package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class UpdateCycleRequest {
    private long id;
    private String name;
    private Integer yearFrom;
    private Integer yearTo;
    private Boolean active;
    private Long meinMonoVersionId;
    private Long meinVersionId;
}
