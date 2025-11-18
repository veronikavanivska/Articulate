-- flyway:executeInTransaction=false

-- speeds up DELETE ... WHERE version_id=? on mein_journal
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mein_journal_version
    ON mein_journal (version_id);

-- speeds up cascade from mein_journal -> mein_journal_code (by journal_id)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mein_journal_code_journal
    ON mein_journal_code (journal_id);
