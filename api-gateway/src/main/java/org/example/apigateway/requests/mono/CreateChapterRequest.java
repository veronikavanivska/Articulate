package org.example.apigateway.requests.mono;

import lombok.Data;
import org.example.apigateway.requests.articles.Coauthors;
import org.example.apigateway.responses.articles.Coauthor;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.RefItem;

import java.util.List;

@Data
public class CreateChapterRequest {

    private Long typeId;
    private Long disciplineId;

    private String monograficChapterTitle;
    private String monograficTitle;
    private String monographPublisher;
    private String doi;
    private String isbn;
    private Integer publicationYear;
    private List<Coauthors> coauthor;
}
