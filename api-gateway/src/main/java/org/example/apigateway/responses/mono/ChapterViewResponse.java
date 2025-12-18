package org.example.apigateway.responses.mono;

import lombok.Data;
import org.example.apigateway.requests.articles.Coauthors;
import org.example.apigateway.responses.articles.Coauthor;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.RefItem;

import java.util.List;

@Data
public class ChapterViewResponse {

    private Long id;
    private Long authorId;

    private RefItem type;
    private RefItem discipline;
    private CycleItem cycle;

    private String monograficChapterTitle;
    private String monograficTitle;
    private String monographPublisher;
    private String doi;
    private String isbn;

    private Double points;

    private Long meinMonoPublisherId;
    private Long meinMonoId;

    private List<Coauthor> coauthor;
}
