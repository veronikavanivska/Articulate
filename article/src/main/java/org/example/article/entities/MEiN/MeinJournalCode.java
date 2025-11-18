package org.example.article.entities.MEiN;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;

import java.io.Serializable;

@Data
@Entity
@Table(name = "mein_journal_code")
public class MeinJournalCode {
//    @EmbeddedId
//    private Id id;
////
////    @MapsId("versionId")
////    @ManyToOne(fetch=FetchType.LAZY)
////    @JoinColumn(name="version_id", nullable=false)
////    private MeinVersion version;
////
////    @MapsId("journalId")
////    @ManyToOne(fetch=FetchType.LAZY)
////    @JoinColumn(name="journal_id", nullable=false)
////    private MeinJournal journal;
////
////    @MapsId("code")
////    @ManyToOne(fetch=FetchType.LAZY)
////    @JoinColumn(name="code", nullable=false)
////    private MeinCode codeRef;
//// (journal_id, version_id) -> MeinJournal(id, version_id)
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @MapsId("journalKey")
//    @JoinColumns({
//            @JoinColumn(name = "journal_id", referencedColumnName = "id",         nullable = false),
//            @JoinColumn(name = "version_id", referencedColumnName = "version_id", nullable = false)
//    })
//    private MeinJournal journal;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @MapsId("code") // третя складова PK
//    @JoinColumn(name = "code", nullable = false)
//    private MeinCode codeRef;
//
////    @Embeddable
////    public static class Id implements java.io.Serializable {
////        @Column(name="version_id") private Long versionId;
////        @Column(name="journal_id") private Long journalId;
////        @Column(name="code", length=16) private String code;
////    }
//@Getter @Setter
//@Embeddable
//@EqualsAndHashCode
//public static class JournalKey implements Serializable {
//    @Column(name = "journal_id", nullable = false) private Long journalId;
//    @Column(name = "version_id", nullable = false) private Long versionId;
//}
//
//    @Getter @Setter
//    @Embeddable
//    @EqualsAndHashCode
//    public static class Id implements Serializable {
//        @AttributeOverrides({
//                @AttributeOverride(name = "journalId", column = @Column(name = "journal_id", nullable = false)),
//                @AttributeOverride(name = "versionId", column = @Column(name = "version_id", nullable = false))
//        })
//        private JournalKey journalKey;
//
//        @Column(name = "code", length = 16, nullable = false)
//        private String code;
//    }
@EmbeddedId
private Id id;

    // FK: (journal_id, version_id) -> mein_journal(id, version_id)
    // Ці колонки вже є у складі PK, тому тут ставимо insertable/updatable = false,
    // щоб уникнути дублювання мапінгу тих самих колонок.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "journal_id", referencedColumnName = "id",
                    nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "version_id", referencedColumnName = "version_id",
                    nullable = false, insertable = false, updatable = false)
    })
    private MeinJournal journal;

    // Частина PK — code. Так само робимо зв'язок read-only
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
