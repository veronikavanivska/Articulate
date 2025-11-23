package org.example.article.entities.MEiN.article;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "mein_version")
public class MeinVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String sourceFilename;
    private String sourceSha256;
    private Long importedBy;
    @Column(name = "imported_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private Instant importedAt;

    @Column(name="is_active")
    private boolean active;
}
