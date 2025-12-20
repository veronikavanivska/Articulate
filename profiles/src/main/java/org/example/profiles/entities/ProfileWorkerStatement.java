package org.example.profiles.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "profile_worker_statement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileWorkerStatement {

    @EmbeddedId
    private ProfileWorkerStatementId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Discipline discipline;

    @Column(name = "fte", nullable = false, precision = 6, scale = 4)
    private BigDecimal fte = new BigDecimal("1.0000");

    @Column(name = "share_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal sharePercent = new BigDecimal("100.00");

    @Column(name = "slot_in_discipline", nullable = false, precision = 8, scale = 4)
    private BigDecimal slotInDiscipline = new BigDecimal("1.0000");

    @Column(name = "max_slots", nullable = false, precision = 8, scale = 4)
    private BigDecimal maxSlots = new BigDecimal("1.0000");

    @Column(name = "max_mono_slots", nullable = false, precision = 8, scale = 4)
    private BigDecimal maxMonoSlots = new BigDecimal("0.0000");

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}