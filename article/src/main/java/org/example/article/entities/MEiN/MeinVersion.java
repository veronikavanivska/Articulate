package org.example.article.entities.MEiN;

import com.google.type.DateTime;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

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
