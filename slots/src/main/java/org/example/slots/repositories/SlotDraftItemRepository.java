package org.example.slots.repositories;

import org.example.slots.entities.PublicationKind;
import org.example.slots.entities.SlotDraftItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface SlotDraftItemRepository extends JpaRepository<SlotDraftItem, Long> {

    boolean existsByDraft_IdAndKindAndPublicationId(Long draftId, PublicationKind kind, Long publicationId);

    List<SlotDraftItem> findByDraft_IdOrderByIdAsc(Long draftId);

    @Transactional
    void deleteByDraft_IdAndKindAndPublicationId(Long draftId, PublicationKind kind, Long publicationId);

    @Query("select coalesce(sum(i.slotValue), 0) from SlotDraftItem i where i.draft.id = :draftId")
    BigDecimal sumSlotValue(@Param("draftId") Long draftId);

    @Query("""
        select coalesce(sum(i.slotValue), 0)
        from SlotDraftItem i
        where i.draft.id = :draftId
          and i.kind in (org.example.slots.entities.PublicationKind.MONOGRAPH,
                         org.example.slots.entities.PublicationKind.CHAPTER)
    """)
    BigDecimal sumSlotValueMono(@Param("draftId") Long draftId);

    @Query("select coalesce(sum(i.points), 0) from SlotDraftItem i where i.draft.id = :draftId")
    BigDecimal sumPoints(@Param("draftId") Long draftId);

    @Query("select coalesce(sum(i.pointsRecalc), 0) from SlotDraftItem i where i.draft.id = :draftId")
    BigDecimal sumPointsRecalc(@Param("draftId") Long draftId);

    @Query("""
           select i
           from SlotDraftItem i
           join fetch i.draft d
           where i.kind = :kind and i.publicationId = :publicationId
           """)
    @Transactional
    List<SlotDraftItem> findAllByKindAndPublicationIdFetchDraft(
            @Param("kind") PublicationKind kind,
            @Param("publicationId") Long publicationId
    );

    /**
     * Idempotent delete bez ładowania encji.
     * Zwraca ile rekordów skasowano.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from SlotDraftItem i where i.kind = :kind and i.publicationId = :publicationId")
    int deleteAllByKindAndPublicationId(
            @Param("kind") PublicationKind kind,
            @Param("publicationId") Long publicationId
    );
}

