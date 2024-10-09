CREATE TABLE assessment_event
(
    id SERIAL NOT NULL constraint assessment_event_pk PRIMARY KEY,
    assessment_id INTEGER NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    summary VARCHAR(512) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    changes jsonb NULL,
    constraint fk_assessment_event_assessment foreign key (assessment_id) references assessment(id)
);


ALTER TABLE assessment_event
    ADD CONSTRAINT assessment_event_event_type_chk CHECK ( event_type IN (
                                                            'STATUS_CHANGE'
        ));

CREATE INDEX idx_assessment_event_assessment_id ON assessment_event(assessment_id);
CREATE INDEX idx_assessment_event_event_time ON assessment_event(event_time);
