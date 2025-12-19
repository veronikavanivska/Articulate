package org.example.slots.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "slot_draft_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_slot_item",
                columnNames = {"draft_id", "kind", "publication_id"}
        )
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotDraftItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", nullable = false)
    private SlotDraft draft;

    @Column(name = "publication_id", nullable = false)
    private Long publicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private PublicationKind kind;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "points", nullable = false, precision = 10, scale = 4)
    private BigDecimal points;

    @Column(name = "slot_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal slotValue;

    @Column(name = "points_recalc", nullable = false, precision = 10, scale = 4)
    private BigDecimal pointsRecalc;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
