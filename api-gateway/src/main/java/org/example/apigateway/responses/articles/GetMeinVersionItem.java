package org.example.apigateway.responses.articles;

import lombok.Data;

@Data
public class GetMeinVersionItem {
    private MeinVersionItem meinVersion;
    private long distinctIssn;
    private long distinctEissn;
    private long distinctCodes;
}
