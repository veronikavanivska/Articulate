package org.example.article.entities.MEiN;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
@Table(name = "mein_journal_code")
public class MeinJournalCode {
    @EmbeddedId
    private Id id;

    @MapsId("versionId")
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="version_id", nullable=false)
    private MeinVersion version;

    @MapsId("journalId")
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="journal_id", nullable=false)
    private MeinJournal journal;

    @MapsId("code")
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="code", nullable=false)
    private MeinCode codeRef;

    @Embeddable
    public static class Id implements java.io.Serializable {
        @Column(name="version_id") private Long versionId;
        @Column(name="journal_id") private Long journalId;
        @Column(name="code", length=16) private String code;
    }
}
