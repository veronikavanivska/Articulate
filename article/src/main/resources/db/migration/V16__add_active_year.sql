ALTER TABLE eval_cycle
    ADD COLUMN IF NOT EXISTS active_year INT;

-- active_year ma być w zakresie cyklu
ALTER TABLE eval_cycle
    ADD CONSTRAINT ck_eval_cycle_active_year_range
        CHECK (active_year IS NULL OR (active_year BETWEEN year_from AND year_to));

-- jeśli cykl jest aktywny, active_year musi być ustawiony
ALTER TABLE eval_cycle
    ADD CONSTRAINT ck_eval_cycle_active_year_required
        CHECK (NOT is_active OR active_year IS NOT NULL);

-- tylko jeden aktywny cykl naraz
CREATE UNIQUE INDEX IF NOT EXISTS uq_eval_cycle_one_active
    ON eval_cycle (is_active)
    WHERE is_active;
