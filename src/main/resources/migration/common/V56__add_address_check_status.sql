ALTER TABLE assessment ADD COLUMN address_checks_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
    CONSTRAINT address_checks_status_chk CHECK(address_checks_status in ('NOT_STARTED',
                                                                         'IN_PROGRESS',
                                                                         'UNSUITABLE',
                                                                         'SUITABLE'
        ));
