package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class PageMeta {
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
