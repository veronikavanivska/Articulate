package org.example.apigateway.responses.articles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeinVersionItem {
    private long id;
    private String label;
    private String sourceFilename;
    private long importedBy;
    private Instant importedAt;
    private boolean isActive;
    private long journals;
    private long journalCodes;
}
