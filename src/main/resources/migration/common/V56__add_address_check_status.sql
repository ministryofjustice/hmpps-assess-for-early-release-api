ALTER TABLE assessment ADD COLUMN address_checks_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
    CONSTRAINT address_checks_status_chk CHECK(status in ('NOT_STARTED'));
