package org.example.apigateway.requests.articles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePublicationRequest {
    private long typeId;
    private long disciplineId;
    private String title;
    private String doi;
    private String issn;
    private String eissn;
    private String journalTitle;
    private int publicationYear;
    private List<Coauthors> coauthors;
}
