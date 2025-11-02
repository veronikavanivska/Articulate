ALTER TABLE eval_cycle ADD COLUMN mein_version_id BIGINT NULL REFERENCES mein_version(id);

