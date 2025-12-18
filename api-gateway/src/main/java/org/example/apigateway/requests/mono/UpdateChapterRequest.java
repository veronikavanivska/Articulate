package org.example.apigateway.requests.mono;

import lombok.Data;
import org.example.apigateway.requests.articles.Coauthors;

import java.util.List;

@Data
public class UpdateChapterRequest {
    private Long id;

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
