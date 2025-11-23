package org.example.article.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.example.article.entities.MEiN.article.MeinCode;

@Entity
@Data
@Table(name = "discipline_mein_code")
public class DisciplineMeinCode {
    @EmbeddedId
    private Id id;

    @MapsId("disciplineId")
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="discipline_id", nullable=false)
    private Discipline discipline;

    @MapsId("meinCode")
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="mein_code", nullable=false)
    private MeinCode meinCodeRef;

    @Embeddable
    public static class Id implements java.io.Serializable {
        @Column(name="discipline_id") private Long disciplineId;
        @Column(name="mein_code", length=16) private String meinCode;
    }
}
