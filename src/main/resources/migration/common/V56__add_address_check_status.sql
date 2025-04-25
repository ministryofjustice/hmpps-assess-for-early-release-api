ALTER TABLE assessment ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
    CONSTRAINT assessment_status_chk CHECK(status IN (
                                                      'NOT_STARTED',
                                                      'IN_PROGRESS',
                                                      'UNSUITABLE',
                                                      'SUITABLE'
    ));
