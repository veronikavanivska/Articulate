package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class ListMeinJournalRequest {
    private long versionId;
    private int page;
    private int size;
    private String sortDir;
}
