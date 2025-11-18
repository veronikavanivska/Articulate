package org.example.apigateway.responses.articles;

import lombok.Data;

import java.util.List;

@Data
public class ListPublicationResponse {
    public List<PublicationViewResponse> publications;
    public PageMeta pageMeta;
}
