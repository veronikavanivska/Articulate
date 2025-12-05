package org.example.article.entities.MEiN.monographs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.article.entities.Discipline;
import org.example.article.entities.EvalCycle;
import org.example.article.entities.PublicationType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;



@Data
@Table(name="monographic")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monographic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private PublicationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_id", nullable = false)
    private Discipline discipline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private EvalCycle cycle;


    @OneToMany(mappedBy = "monograph", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MonographAuthor> coauthors = new ArrayList<>();

    private String doi;
    private String isbn;
    private String title;
    private String  monograficTitle;
    private Integer publicationYear;
    private Integer meinPoints;

    private Long meinMonoPublisherId;
    private Long meinMonoId;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


}
