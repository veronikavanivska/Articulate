package org.example.slots.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "slot_draft",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_slot_draft",
                columnNames = {"user_id", "eval_cycle_id", "discipline_id", "eval_year"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "eval_cycle_id", nullable = false)
    private Long evalCycleId;

    @Column(name = "discipline_id", nullable = false)
    private Long disciplineId;

    @Column(name = "eval_year", nullable = false)
    private Integer evalYear;

    @Column(name = "max_slots", nullable = false, precision = 8, scale = 4)
    private BigDecimal maxSlots;

    @Column(name = "max_mono_slots", nullable = false, precision = 8, scale = 4)
    private BigDecimal maxMonoSlots = new BigDecimal("0.0000");

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (maxMonoSlots == null) maxMonoSlots = new BigDecimal("0.0000");
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
