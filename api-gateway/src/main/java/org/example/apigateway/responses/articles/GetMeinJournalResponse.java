package org.example.apigateway.responses.articles;

import lombok.Data;

import java.util.List;


@Data
public class GetMeinJournalResponse {
    private Long id;
    private String uid;
    private String title1;
    private String title2;
    private String issn;
    private String issn2;
    private String eissn;
    private String eissn2;
    private int points;
    private List<CodeRef> codes;

    @Data
    public static class CodeRef {
        private String code;
        private String name;
    }
}
