CREATE TABLE address_deletion_event
(
    id SERIAL NOT NULL CONSTRAINT address_deletion_event_pk PRIMARY KEY,
    reason_type VARCHAR(40)
        CONSTRAINT reason_type_chk CHECK
            (
                reason_type IN (
                                               'NO_LONGER_WANTS_TO_BE_RELEASED_HERE',
                                               'NOT_ENOUGH_TIME_TO_ASSESS',
                                               'HAS_ANOTHER_SUITABLE_ADDRESS',
                                               'OTHER_REASON'
                )
            ),
    other_reason VARCHAR(255),
    assessment_event_id BIGINT NULL,
    CONSTRAINT fk_address_deletion_event FOREIGN KEY (assessment_event_id) REFERENCES assessment_event(id)
);
