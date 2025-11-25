ALTER TABLE publication_coauthor
    ADD COLUMN user_id BIGINT NULL,
    ADD COLUMN is_internal BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS ix_pub_coauthor_user
    ON publication_coauthor(user_id);
