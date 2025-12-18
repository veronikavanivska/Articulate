-- 1) tabela łącząca: jeden worker może mieć wiele dyscyplin
CREATE TABLE IF NOT EXISTS profile_worker_discipline (
                                                         user_id       BIGINT NOT NULL
                                                         REFERENCES profile_worker(user_id) ON DELETE CASCADE,
    discipline_id BIGINT NOT NULL
    REFERENCES discipline(id) ON DELETE RESTRICT,

    is_primary    BOOLEAN NOT NULL DEFAULT FALSE,

    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, discipline_id)
    );

CREATE INDEX IF NOT EXISTS idx_pwd_discipline
    ON profile_worker_discipline(discipline_id);

CREATE TABLE IF NOT EXISTS profile_worker_statement (
                                                        user_id       BIGINT NOT NULL
                                                        REFERENCES profile_worker(user_id) ON DELETE CASCADE,
    discipline_id BIGINT NOT NULL
    REFERENCES discipline(id) ON DELETE RESTRICT,
    eval_year     INT NOT NULL,

    fte           NUMERIC(6,4) NOT NULL DEFAULT 1.0000,    -- etat
    share_percent NUMERIC(6,2) NOT NULL DEFAULT 100.00,    -- udział %
    slot_in_discipline NUMERIC(8,4) NOT NULL DEFAULT 0.0000, -- "slot w dysc."

    max_slots     NUMERIC(8,4) NOT NULL DEFAULT 1.0000,    -- to Twoje "sloty do wypełnienia"
    max_mono_slots NUMERIC(8,4) NOT NULL DEFAULT 0.0000,   -- jeśli masz osobny limit

    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, discipline_id, eval_year)
    );

CREATE INDEX IF NOT EXISTS idx_pws_lookup
    ON profile_worker_statement(eval_year, discipline_id, user_id);
