package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class AdminListPublicationRequest {
    private long id;
    private int typeId;
    private int disciplineId;
    private int cycleId;
    private int page;
    private int size;
    private String sortBy;
    private String sortDir;
}
