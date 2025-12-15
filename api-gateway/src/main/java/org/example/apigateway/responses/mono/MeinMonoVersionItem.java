package org.example.apigateway.responses.mono;

import lombok.Data;

import java.time.Instant;

@Data
public class MeinMonoVersionItem {
    private long id;
    private String label;
    private String sourceFilename;
    private long importedBy;
    private Instant importedAt;
    private long publishers;
}
