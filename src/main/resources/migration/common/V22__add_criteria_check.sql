CREATE TABLE criteria_check
(
    id SERIAL NOT NULL constraint criteria_check_pk PRIMARY KEY,
    assessment_id INTEGER NOT NULL,
    criteria_code VARCHAR(64) NOT NULL,
    criteria_version VARCHAR(16) NOT NULL,
    criteria_met BOOLEAN NOT NULL,
    criteria_type VARCHAR(16) NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    question_answers jsonb NULL,
    constraint fk_criteria_check_assessment foreign key (assessment_id) references assessment(id)
);
