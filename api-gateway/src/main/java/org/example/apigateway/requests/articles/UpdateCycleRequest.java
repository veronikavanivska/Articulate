package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class UpdateCycleRequest {
    private long id;
    private String name;
    private int yearFrom;
    private int yearTo;
    private boolean active;
    private long meinMonoVersionId;
    private long meinVersionId;
}
