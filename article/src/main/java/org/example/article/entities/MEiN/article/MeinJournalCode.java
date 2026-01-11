package org.example.article.entities.MEiN.article;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Entity
@Table(name = "mein_journal_code")
public class MeinJournalCode {

@EmbeddedId
private Id id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "journal_id", referencedColumnName = "id",
                    nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "version_id", referencedColumnName = "version_id",
                    nullable = false, insertable = false, updatable = false)
    })
    private MeinJournal journal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "code", nullable = false,
            insertable = false, updatable = false)
    private MeinCode codeRef;

    @Getter @Setter
    @Embeddable
    @EqualsAndHashCode
    public static class Id implements Serializable {
        @Column(name = "version_id", nullable = false)
        private Long versionId;

        @Column(name = "journal_id", nullable = false)
        private Long journalId;

        @Column(name = "code", length = 16, nullable = false)
        private String code;
    }
}
