package org.example.profiles.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "profile_worker_discipline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileWorkerDiscipline {

    @EmbeddedId
    private ProfileWorkerDisciplineId id;

    // wygodne gettery
    @Transient
    public Long getUserId() { return id != null ? id.getUserId() : null; }

    @Transient
    public Long getDisciplineId() { return id != null ? id.getDisciplineId() : null; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Discipline discipline;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}