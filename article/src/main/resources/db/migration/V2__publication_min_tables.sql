CREATE TABLE IF NOT EXISTS eval_cycle (
                                          id         BIGSERIAL PRIMARY KEY,
                                          name       TEXT    NOT NULL,
                                          year_from  INT     NOT NULL,
                                          year_to    INT     NOT NULL,
                                          is_active  BOOLEAN NOT NULL DEFAULT FALSE,
                                          CONSTRAINT ck_cycle_range CHECK (year_from <= year_to)
    );


CREATE TABLE IF NOT EXISTS publication_type (
                                                id    BIGSERIAL PRIMARY KEY,
                                                name  VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS discipline (
                                          id     BIGSERIAL PRIMARY KEY,
                                          name   TEXT NOT NULL

);

CREATE TABLE IF NOT EXISTS publication (
    id               BIGSERIAL PRIMARY KEY,
    author_id         BIGINT NOT NULL,

    type_id          BIGINT NOT NULL REFERENCES publication_type(id),
    title            TEXT   NOT NULL,
    doi              TEXT,
    issn             TEXT,
    eissn            TEXT,
    journal_title    TEXT,

    publication_year INT    NOT NULL,

    cycle_id         BIGINT NOT NULL REFERENCES eval_cycle(id),
    discipline_id    BIGINT NOT NULL REFERENCES discipline(id),

    mein_points      INT,
    mein_version_id  BIGINT REFERENCES mein_version(id),
    mein_journal_id  BIGINT REFERENCES mein_journal(id),

    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
    );
CREATE UNIQUE INDEX IF NOT EXISTS ux_pub_doi   ON publication(doi) WHERE doi IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_pub_author       ON publication(author_id);
CREATE INDEX IF NOT EXISTS ix_pub_year         ON publication(publication_year);
CREATE INDEX IF NOT EXISTS ix_pub_cycle        ON publication(cycle_id);
CREATE INDEX IF NOT EXISTS ix_pub_discipline   ON publication(discipline_id);
CREATE INDEX IF NOT EXISTS ix_pub_type         ON publication(type_id);

CREATE TABLE IF NOT EXISTS publication_coauthor (
                                                    id             BIGSERIAL PRIMARY KEY,
                                                    publication_id BIGINT NOT NULL REFERENCES publication(id) ON DELETE CASCADE,
    position       INT    NOT NULL,   -- kolejność autora (1..n)
    full_name      TEXT   NOT NULL
    );