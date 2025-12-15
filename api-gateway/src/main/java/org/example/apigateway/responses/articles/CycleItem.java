package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class CycleItem {
    private long id;
    private int yearFrom;
    private int yearTo;
    private String name;
    private boolean isActive;
    private long meinVersionId;
    private long meinMonoVersionId;
}

