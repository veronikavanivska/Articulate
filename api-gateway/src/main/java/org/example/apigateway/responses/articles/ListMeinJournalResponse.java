package org.example.apigateway.responses.articles;

import lombok.Data;

import java.util.List;

@Data
public class ListMeinJournalResponse {
    private List<MeinJournalItem> meinJournals;
    PageMeta pageMeta;
}
