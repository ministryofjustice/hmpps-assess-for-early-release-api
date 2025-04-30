ALTER TABLE assessment
    ADD COLUMN eligibility_checks_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
        CONSTRAINT eligibility_checks_status_chk CHECK (eligibility_checks_status in ('ELIGIBLE',
                                                                                      'INELIGIBLE',
                                                                                      'IN_PROGRESS',
                                                                                      'NOT_STARTED'
            ));
