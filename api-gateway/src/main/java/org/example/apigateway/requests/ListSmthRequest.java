package org.example.apigateway.requests;

import lombok.Data;

@Data
public class ListSmthRequest {
    private int page;
    private int size;
    private String sortDir;
}
