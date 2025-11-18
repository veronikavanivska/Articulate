package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class GetMeinJournalRequest {
    private long versionId;
    private long journalId;
}
