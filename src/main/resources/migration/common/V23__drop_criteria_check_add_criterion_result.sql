DROP TABLE criteria_check;

CREATE TABLE eligibility_check_result(
    id SERIAL NOT NULL constraint criteria_check_pk PRIMARY KEY,
    assessment_id INTEGER NOT NULL,
    criterion_code VARCHAR(64) NOT NULL,
    criterion_version VARCHAR(16) NOT NULL,
    criterion_met BOOLEAN NOT NULL,
    criterion_type VARCHAR(16) NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    question_answers jsonb NULL,
    constraint fk_eligibility_check_assessment foreign key (assessment_id) references assessment(id)
);
