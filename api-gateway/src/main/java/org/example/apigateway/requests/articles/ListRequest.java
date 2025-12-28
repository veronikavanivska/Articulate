package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class ListRequest {
    private Long typeId;
    private Long disciplineId;
    private Long cycleId;
    private String title;

    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}
