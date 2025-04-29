CREATE TABLE assessment_to_last_update_event (  assessment_id  int8 NOT NULL,
                                                event_id       int8 NOT NULL,
        CONSTRAINT assessment_to_last_update_event_pkey PRIMARY KEY (assessment_id, event_id),
        CONSTRAINT assessment_must_exist                FOREIGN KEY (assessment_id) REFERENCES assessment(id),
        CONSTRAINT assessment_event_must_exist          FOREIGN KEY (event_id) REFERENCES assessment_event(id)
);