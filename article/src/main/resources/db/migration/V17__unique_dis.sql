ALTER TABLE discipline
    ADD CONSTRAINT uq_article_discipline_name UNIQUE (name);