package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class RecalcCycleScoresResponse {
    private long updated_publications;
    private long unmatched_publications;
}
