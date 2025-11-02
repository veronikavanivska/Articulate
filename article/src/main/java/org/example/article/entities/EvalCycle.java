package org.example.article.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.example.article.entities.MEiN.MeinVersion;

@Data
@Entity
@Table(name = "eval_cycle")
public class EvalCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer yearFrom;

    @Column(nullable = false)
    private Integer yearTo;

    @Column(nullable = false)
    private boolean isActive = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "mein_version_id")
    private MeinVersion meinVersion;
}
