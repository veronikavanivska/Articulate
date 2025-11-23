package org.example.article.entities.MEiN.article;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "mein_journal")
public class MeinJournal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="version_id", nullable=false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MeinVersion version;

    private Integer lp;
    private String uid;

    @Column(name = "title_1")
    private String title1;
    private String issn;
    private String eissn;

    @Column(name = "title_2")
    private String title2;
    private String issn2;
    private String eissn2;

   private Integer points;
}
