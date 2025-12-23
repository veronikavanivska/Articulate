package org.example.apigateway.requests;

import lombok.Data;

@Data
public class GetDraftRequest {
    private long disciplineId;
    private int evalYear;
    private long evalCycle;
}
