package org.example.slots.repositories;

import org.example.slots.entities.SlotDraftItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SlotDraftItemRepository extends JpaRepository<SlotDraftItem, Long> {
    boolean existsByDraftIdAndPublicationId(Long draftId, Long publicationId);

    List<SlotDraftItem> findByDraftIdOrderByIdAsc(Long draftId);

    void deleteByDraftIdAndPublicationId(Long draftId, Long publicationId);

    @Query("select coalesce(sum(i.slotValue), 0) from SlotDraftItem i where i.draft.id = :draftId")
    BigDecimal sumSlotValue(@Param("draftId") Long draftId);
}
