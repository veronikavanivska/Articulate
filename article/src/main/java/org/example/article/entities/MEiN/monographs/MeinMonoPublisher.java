package org.example.article.entities.MEiN.monographs;

import jakarta.persistence.*;
import lombok.Data;
import org.example.article.entities.MEiN.article.MeinVersion;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Table(name = "mein_mono_publisher")
@Entity
public class MeinMonoPublisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="version_id", nullable=false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MeinMonoVersion version;

    private int lp;
    private String uid;
    private String name;
    private int points;
    private String level;

}
