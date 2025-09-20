CREATE TABLE IF NOT EXISTS discipline_mein_code (
    discipline_id BIGINT NOT NULL REFERENCES discipline(id) ON DELETE CASCADE,
    mein_code     TEXT   NOT NULL REFERENCES mein_code(code) ON DELETE RESTRICT,
    PRIMARY KEY (discipline_id, mein_code)
    );

CREATE INDEX IF NOT EXISTS idx_dmc_mein_code ON discipline_mein_code(mein_code);
