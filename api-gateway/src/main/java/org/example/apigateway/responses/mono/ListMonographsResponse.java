package org.example.apigateway.responses.mono;

import lombok.Data;
import org.example.apigateway.mappers.MonographViewMapper;
import org.example.apigateway.responses.articles.PageMeta;
import org.example.apigateway.responses.articles.PublicationViewResponse;

import java.util.List;

@Data
public class ListMonographsResponse {
    public List<MonographViewResponse> monograph;
    public PageMeta pageMeta;
}
