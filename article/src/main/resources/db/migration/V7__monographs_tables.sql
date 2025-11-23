-- One version = one uploaded monograph list (PDF/XLSX), for a given MEiN cycle.
CREATE TABLE IF NOT EXISTS mein_mono_version (
    id              BIGSERIAL PRIMARY KEY,
    label           TEXT        NOT NULL,                -- e.g. 'Wykaz wydawnictw 2021'
    source_filename TEXT        NOT NULL,                -- pdf/xlsx filename
    source_sha256   TEXT        NOT NULL UNIQUE,         -- idempotency per physical file
    imported_by     BIGINT      NOT NULL,                -- your user id
    imported_at     TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS mein_mono_publisher (
    id          BIGSERIAL PRIMARY KEY,
    version_id  BIGINT NOT NULL REFERENCES mein_mono_version(id) ON DELETE CASCADE,

    lp          INT,            -- Lp. from the list
    uid         TEXT,           -- Unikatowy Identyfikator Wydawnictwa
    name        TEXT NOT NULL,  -- Nazwa wydawnictwa
    points      INT  NOT NULL,  -- 200 / 80 etc.
    level       TEXT            -- optional: 'I','II' if you want to store "POZIOM I/II"
    );

CREATE UNIQUE INDEX IF NOT EXISTS uniq_mein_mono_publisher_uid_per_ver
    ON mein_mono_publisher(version_id, uid) WHERE uid IS NOT NULL;

ALTER TABLE eval_cycle
    ADD COLUMN mein_monograph_version_id BIGINT NULL REFERENCES mein_mono_version(id);

CREATE TABLE IF NOT EXISTS monographic(
    id              BIGSERIAL PRIMARY KEY,
    author_id         BIGINT NOT NULL,

    type_id          BIGINT NOT NULL REFERENCES publication_type(id),
    discipline_id    BIGINT NOT NULL REFERENCES discipline(id),

    title            TEXT   NOT NULL,
    doi              TEXT,
    isbn             TEXT,

    publication_year INT    NOT NULL,
    monografic_title    TEXT,

    mein_points      INT,
    mein_mono_publisher_id BIGINT              REFERENCES mein_mono_publisher(id) ON DELETE SET NULL,
    mein_mono_id           BIGINT              REFERENCES mein_mono_version(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ DEFAULT now(),
    updated_at    TIMESTAMPTZ DEFAULT now()
);


-- for this points make 1/4*points
CREATE TABLE IF NOT EXISTS monograph_chapter (
    id             BIGSERIAL PRIMARY KEY,
    author_id         BIGINT NOT NULL,

    type_id          BIGINT NOT NULL REFERENCES publication_type(id),
    discipline_id    BIGINT NOT NULL REFERENCES discipline(id),

    monografic_title    TEXT NOT NULL,
    monografic_chapter_title    TEXT NOT NULL,

    publication_year INT    NOT NULL,
    doi              TEXT,
    isbn             TEXT,

    mein_points      INT,
    mein_mono_publisher_id BIGINT              REFERENCES mein_mono_publisher(id) ON DELETE SET NULL,
    mein_mono_id           BIGINT              REFERENCES mein_mono_version(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ DEFAULT now(),
    updated_at    TIMESTAMPTZ DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS monograph_chapter_author (
    id             BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT  NOT NULL REFERENCES monograph_chapter(id) ON DELETE CASCADE,
    position   INT NOT NULL,
    full_name  TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS monograph_author (
    id             BIGSERIAL PRIMARY KEY,
    monograph_id BIGINT  NOT NULL REFERENCES monographic(id) ON DELETE CASCADE,
    position   INT NOT NULL,
    full_name  TEXT NOT NULL
    );



