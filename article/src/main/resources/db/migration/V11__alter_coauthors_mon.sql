ALTER TABLE monograph_author
    ADD COLUMN user_id BIGINT NULL,
    ADD COLUMN is_internal BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE monograph_chapter_author
    ADD COLUMN user_id BIGINT NULL,
    ADD COLUMN is_internal BOOLEAN NOT NULL DEFAULT FALSE;


CREATE INDEX IF NOT EXISTS ix_mono_author_user
    ON monograph_author(user_id);

CREATE INDEX IF NOT EXISTS ix_mono_chapter_author_user
    ON monograph_chapter_author(user_id);