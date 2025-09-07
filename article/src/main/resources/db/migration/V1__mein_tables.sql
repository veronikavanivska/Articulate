-- each uploaded XLSX = one version (snapshot)
CREATE TABLE IF NOT EXISTS mein_version (
                                            id               BIGSERIAL PRIMARY KEY,
                                            label            TEXT        NOT NULL,
                                            source_filename  TEXT        NOT NULL,
                                            source_sha256    TEXT        NOT NULL UNIQUE,   -- idempotency
                                            imported_by      BIGINT      NOT NULL,
                                            imported_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_active        BOOLEAN     NOT NULL DEFAULT FALSE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uniq_mein_active_version
    ON mein_version ((is_active)) WHERE is_active;

-- dictionary of numeric code columns; 'name' is the Polish text above the code
CREATE TABLE IF NOT EXISTS mein_code (
                                         code        TEXT PRIMARY KEY,            -- e.g. '101','2061','1001'
                                         name        TEXT NOT NULL                    -- optional display order
);

-- journals (a row in the sheet) with single 'Punktacja'
CREATE TABLE IF NOT EXISTS mein_journal (
                                            id          BIGSERIAL PRIMARY KEY,
                                            version_id  BIGINT NOT NULL REFERENCES mein_version(id) ON DELETE CASCADE,
    lp          INT,
    uid         TEXT,                        -- Unikatowy Identyfikator Czasopisma
    title_1     TEXT,               -- Tytuł 1
    issn        TEXT,                        -- 'NNNN-NNNN'
    eissn       TEXT,
    title_2     TEXT,                        -- Tytuł 2
    issn2       TEXT,
    eissn2      TEXT,
    points      INT  NOT NULL                -- Punktacja
    );

CREATE UNIQUE INDEX IF NOT EXISTS uniq_mein_journal_uid_per_ver
    ON mein_journal(version_id, uid)   WHERE uid   IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uniq_mein_journal_issn_per_ver
    ON mein_journal(version_id, issn)  WHERE issn  IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uniq_mein_journal_eissn_per_ver
    ON mein_journal(version_id, eissn) WHERE eissn IS NOT NULL;

-- links: one row per 'x' under a code for a journal
CREATE TABLE IF NOT EXISTS mein_journal_code (
                                                 version_id  BIGINT NOT NULL REFERENCES mein_version(id) ON DELETE CASCADE,
    journal_id  BIGINT NOT NULL REFERENCES mein_journal(id) ON DELETE CASCADE,
    code        TEXT   NOT NULL REFERENCES mein_code(code) ON DELETE RESTRICT,
    PRIMARY KEY (version_id, journal_id, code)
    );

CREATE INDEX IF NOT EXISTS idx_mein_jc_code_ver ON mein_journal_code(code, version_id);
