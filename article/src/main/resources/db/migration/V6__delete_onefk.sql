

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class r ON r.oid = c.conrelid
    WHERE c.conname = 'uq_mein_journal_id_version'
      AND r.relname = 'mein_journal'
  ) THEN
ALTER TABLE mein_journal
    ADD CONSTRAINT uq_mein_journal_id_version UNIQUE (id, version_id);
END IF;
END $$;


UPDATE mein_journal_code c
SET    version_id = j.version_id
    FROM   mein_journal j
WHERE  j.id = c.journal_id
  AND (c.version_id IS DISTINCT FROM j.version_id);

DO $$
DECLARE cnt bigint;
BEGIN
SELECT COUNT(*) INTO cnt
FROM mein_journal_code c
         LEFT JOIN mein_journal j ON j.id = c.journal_id
WHERE j.version_id IS DISTINCT FROM c.version_id;
IF cnt > 0 THEN
    RAISE EXCEPTION 'Data mismatch remains in mein_journal_code: % rows', cnt;
END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class r ON r.oid = c.conrelid
    WHERE r.relname = 'mein_journal_code'
      AND c.conname = 'fk_mjc_journal_version'
  ) THEN
ALTER TABLE mein_journal_code
    ADD CONSTRAINT fk_mjc_journal_version
        FOREIGN KEY (journal_id, version_id)
            REFERENCES mein_journal (id, version_id)
            ON DELETE CASCADE
    NOT VALID;
END IF;
END $$;

ALTER TABLE mein_journal_code
    VALIDATE CONSTRAINT fk_mjc_journal_version;

DO $$
DECLARE
r RECORD;
BEGIN
  -- Шукаємо всі зовнішні ключі з mein_journal_code, що посилаються на mein_version
FOR r IN
SELECT c.conname
FROM pg_constraint c
         JOIN pg_class child   ON child.oid   = c.conrelid
         JOIN pg_class parent  ON parent.oid  = c.confrelid
WHERE c.contype = 'f'
  AND child.relname = 'mein_journal_code'
  AND parent.relname = 'mein_version'
    LOOP
    EXECUTE format('ALTER TABLE mein_journal_code DROP CONSTRAINT %I', r.conname);
END LOOP;
END $$;

ANALYZE mein_journal;
ANALYZE mein_journal_code;