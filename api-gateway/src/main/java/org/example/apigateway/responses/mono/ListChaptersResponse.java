package org.example.apigateway.responses.mono;

import lombok.Data;
import org.example.apigateway.responses.articles.PageMeta;

import java.util.List;

@Data
public class ListChaptersResponse {

    private List<ChapterViewResponse> chapters;
    private PageMeta pageMeta;

}
