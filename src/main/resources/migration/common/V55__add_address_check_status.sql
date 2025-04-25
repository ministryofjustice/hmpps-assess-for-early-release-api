ALTER TABLE assessment ADD COLUMN address_checks_status varchar(64);

ALTER TABLE assessment
    ADD CONSTRAINT assessment_address_checks_status_chk CHECK ( address_checks_status IN (
                                                                              'NOT_STARTED',
                                                                              'IN_PROGRESS',
                                                                              'UNSUITABLE',
                                                                              'SUITABLE'
        ));
