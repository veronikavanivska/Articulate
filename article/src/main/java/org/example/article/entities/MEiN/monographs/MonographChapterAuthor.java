package org.example.article.entities.MEiN.monographs;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "monograph_chapter_author")
public class MonographChapterAuthor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private MonographChapter monographChapter;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private String fullName;

    private Long userId;

    private boolean isInternal;
}
