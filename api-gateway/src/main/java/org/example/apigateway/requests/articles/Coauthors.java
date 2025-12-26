    package org.example.apigateway.requests.articles;

    import lombok.Data;

    @Data
    public class Coauthors {
        private long userId;
        private String fullName;
    }
