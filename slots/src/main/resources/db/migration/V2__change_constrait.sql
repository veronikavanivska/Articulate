ALTER TABLE slot_draft_item
DROP CONSTRAINT IF EXISTS uq_slot_item;

ALTER TABLE slot_draft_item
    ADD CONSTRAINT uq_slot_item UNIQUE (draft_id, kind, publication_id);

CREATE INDEX IF NOT EXISTS idx_slot_item_lookup
    ON slot_draft_item (draft_id, kind, publication_id);
