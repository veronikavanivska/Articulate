package org.example.article.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class AsyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // np. "RECALC_CYCLE_SCORES", "DELETE_MEIN_VERSION"
    private String type;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Integer progressPercent;
    private String phase;              // np. "Deleting journals", "Recalculating scores"
    private String message;            // krótki opis dla UI
    @Column(name = "business_key", length = 200)
    private String businessKey;
    @Column(columnDefinition = "text")
    private String requestPayload;
    @Column(columnDefinition = "text")
    private String resultPayload;

    private String errorMessage;

    private Instant createdAt;
    private Instant finishedAt;

    public enum Status {
        QUEUED,
        RUNNING,
        DONE,
        FAILED
    }

}