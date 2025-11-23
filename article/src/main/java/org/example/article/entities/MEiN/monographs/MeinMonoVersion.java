package org.example.article.entities.MEiN.monographs;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Table(name = "mein_mono_version")
@Entity
public class MeinMonoVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    private String label;
    private String sourceFilename;
    private String sourceSha256;
    private Long importedBy;
    @Column(name = "imported_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private Instant importedAt;


}
