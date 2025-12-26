package org.example.apigateway.responses;

import lombok.Data;

import java.util.List;

@Data
public class ListProfileResponse {
    private List<ListProfileItem> items;
    private Long total;
    private Integer totalPages;
}
