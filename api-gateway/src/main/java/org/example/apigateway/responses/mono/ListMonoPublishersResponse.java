package org.example.apigateway.responses.mono;

import lombok.Data;
import org.example.apigateway.responses.articles.PageMeta;

import java.util.List;

@Data
public class ListMonoPublishersResponse {
    private List<MeinMonoPublisherItem> items;
    PageMeta pageMeta;
}

