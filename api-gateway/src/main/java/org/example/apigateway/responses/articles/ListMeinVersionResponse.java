package org.example.apigateway.responses.articles;

import lombok.Data;

import java.util.List;

@Data
public class ListMeinVersionResponse {
    List<MeinVersionItem> item;
    PageMeta pageMeta;
}
