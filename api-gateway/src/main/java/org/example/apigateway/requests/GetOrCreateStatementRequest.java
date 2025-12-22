package org.example.apigateway.requests;

import lombok.Data;

@Data
public class GetOrCreateStatementRequest {
    long userId;
    long disciplineId;
    int evalYear;
}
