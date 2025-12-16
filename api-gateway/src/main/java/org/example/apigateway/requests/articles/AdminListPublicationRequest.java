package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class AdminListPublicationRequest {
    private Long id;
    private Integer typeId;
    private Integer disciplineId;
    private Integer cycleId;

    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}
