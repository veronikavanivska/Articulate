package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class MeinJournalItem {
    private long id;
    private String uid;
    private String title;
    private String issn;
    private String eissn;
    private int points;
}
