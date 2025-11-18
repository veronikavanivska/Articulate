package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class Coauthor {
    private int position;
    private String fullName;
}
