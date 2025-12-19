CREATE TABLE IF NOT EXISTS slot_draft (
                                          id BIGSERIAL PRIMARY KEY,

                                          user_id BIGINT NOT NULL,
                                          eval_cycle_id BIGINT NOT NULL,
                                          discipline_id BIGINT NOT NULL,
                                          eval_year INT NOT NULL,

                                          max_slots NUMERIC(8,4) NOT NULL,
    max_mono_slots NUMERIC(8,4) NOT NULL DEFAULT 0.0000,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_slot_draft UNIQUE (user_id, eval_cycle_id, discipline_id, eval_year)
    );

CREATE INDEX IF NOT EXISTS idx_slot_draft_lookup
    ON slot_draft (user_id, eval_cycle_id, discipline_id, eval_year);


CREATE TABLE IF NOT EXISTS slot_draft_item (
                                               id BIGSERIAL PRIMARY KEY,

                                               draft_id BIGINT NOT NULL REFERENCES slot_draft(id) ON DELETE CASCADE,

    publication_id BIGINT NOT NULL,
    kind TEXT NOT NULL,                 -- ARTICLE / CHAPTER / MONOGRAPH
    publication_year INT NOT NULL,

    title TEXT NOT NULL,
    points NUMERIC(10,4) NOT NULL,

    slot_value NUMERIC(10,4) NOT NULL,
    points_recalc NUMERIC(10,4) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_slot_item UNIQUE (draft_id, publication_id),
    CONSTRAINT chk_slot_value_nonneg CHECK (slot_value >= 0),
    CONSTRAINT chk_points_nonneg CHECK (points >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_slot_item_draft ON slot_draft_item (draft_id);
CREATE INDEX IF NOT EXISTS idx_slot_item_pub   ON slot_draft_item (publication_id);
