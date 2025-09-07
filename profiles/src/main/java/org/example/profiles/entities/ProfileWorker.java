package org.example.profiles.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "profile_worker")
@Data
public class ProfileWorker {

    @Id
    private Long userId;

    private String degreeTitle;
    private String unitName;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ProfileUser user;

    @PrePersist
    public void prePersist() {
        createdAt  = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
