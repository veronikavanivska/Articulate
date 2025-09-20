package org.example.article.entities.MEiN;

import jakarta.persistence.*;
import lombok.Data;

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

    @Column(name="is_active")
    private boolean active;
}
