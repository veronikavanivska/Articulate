package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class CreateCycleRequest {
    private String name;
    private int yearFrom;
    private int yearTo;
    private boolean active;
    private int activeYear;
}
