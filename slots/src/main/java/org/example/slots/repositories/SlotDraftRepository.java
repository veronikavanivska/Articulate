package org.example.slots.repositories;

import org.example.slots.entities.SlotDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlotDraftRepository extends JpaRepository<SlotDraft, Long> {

    Optional<SlotDraft> findByUserIdAndEvalCycleIdAndDisciplineIdAndEvalYear(
            Long userId, Long evalCycleId, Long disciplineId, Integer evalYear
    );
}
