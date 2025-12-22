package org.example.apigateway.responses.mono;

import lombok.Data;
import org.example.apigateway.responses.articles.Coauthor;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.RefItem;

import java.util.List;

@Data
public class MonographViewResponse {
    private Long id;
    private Long authorId;

    private RefItem type;
    private RefItem discipline;
    private CycleItem cycle;

    private String title;
    private String doi;
    private String isbn;
    private Integer points;
    private String monograficTitle;
    private Long meinMonoPublisherId;
    private Long meinMonoId;

    private int publicationYear;

    private List<Coauthor> coauthors;

}
