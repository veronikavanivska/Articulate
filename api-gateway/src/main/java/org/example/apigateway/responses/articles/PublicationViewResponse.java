package org.example.apigateway.responses.articles;

import lombok.Data;

import java.util.List;

@Data
public class PublicationViewResponse {
    private long id;
    private long ownerId;

    private RefItem type;
    private RefItem discipline;
    private CycleItem cycle;

    private String title;
    private String doi;
    private String issn;
    private String eissn;
    private String journalTitle;
    private int publicationYear;

    private Integer meinPoints;
    private Long meinVersionId;
    private Long meinJournalId;

    private List<Coauthor> coauthors;
}
