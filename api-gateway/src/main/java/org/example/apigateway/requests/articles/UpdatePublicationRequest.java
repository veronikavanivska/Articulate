package org.example.apigateway.requests.articles;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePublicationRequest {
    private long id;
    private Long typeId;
    private Long disciplineId;
    private String title;
    private String doi;
    private String issn;
    private String eissn;
    private String journalTitle;
    private Integer publicationYear;
    private List<Coauthors> replaceCoauthors;
}
