CREATE TABLE IF NOT EXISTS discipline (
                                          id   BIGINT PRIMARY KEY,
                                          name TEXT NOT NULL UNIQUE
);

ALTER TABLE profile_worker
    ADD COLUMN discipline_id BIGINT REFERENCES discipline(id);
