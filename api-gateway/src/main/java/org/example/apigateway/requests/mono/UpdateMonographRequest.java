package org.example.apigateway.requests.mono;

import lombok.Data;
import org.example.apigateway.requests.articles.Coauthors;

import java.util.List;

@Data
public class UpdateMonographRequest {
    private Long id;
    private Long typeId;
    private Long disciplineId;
    private String title;
    private String doi;
    private String isbn;
    private String monograficPublisherTitle;
    private Integer publicationYear;
    private List<Coauthors> coauthors;

}
