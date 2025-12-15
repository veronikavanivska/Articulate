package org.example.apigateway.responses.mono;

import lombok.Data;
import org.example.apigateway.responses.articles.PageMeta;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Data
public class ListMonoVersionResponse {
    private List<MeinMonoVersionItem> items;
    private PageMeta pageMeta;
}
