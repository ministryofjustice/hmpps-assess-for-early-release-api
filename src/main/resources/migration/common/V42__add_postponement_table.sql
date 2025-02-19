CREATE TABLE postponement_reason
(
    id SERIAL NOT NULL constraint postponement_reason_pk PRIMARY KEY,
    reason_type varchar(60) NOT NULL,
    assessment_id INTEGER NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    constraint fk_postponement_reason_assessment foreign key (assessment_id) references assessment(id)
);
