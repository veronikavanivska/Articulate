package org.example.article.entities.MEiN.monographs;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "monograph_author")
@Entity
public class MonographAuthor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monograph_id", nullable = false)
    private Monographic monograph;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private String fullName;

    private Long userId;

    private boolean isInternal;
}
