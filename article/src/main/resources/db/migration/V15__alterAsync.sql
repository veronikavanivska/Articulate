ALTER TABLE async_job
    ADD COLUMN business_key VARCHAR(200);

CREATE INDEX idx_async_job_business_key
    ON async_job(business_key);