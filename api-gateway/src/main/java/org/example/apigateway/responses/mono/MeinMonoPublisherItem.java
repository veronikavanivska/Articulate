package org.example.apigateway.responses.mono;

import lombok.Data;

@Data
public class MeinMonoPublisherItem {
    private long id;
    private String title;
    private double points;
    private long versionId;
    private String level;
}
