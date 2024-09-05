CREATE TABLE assessment
(
    id SERIAL NOT NULL constraint assessment_pk PRIMARY KEY,
    offender_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' CONSTRAINT assessment_status_chk CHECK(status in ('NOT_STARTED')),
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    constraint fk_assessment_offender foreign key (offender_id) references offender(id)
);
