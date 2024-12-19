CREATE TABLE residential_checks_task_answer
(
    id SERIAL NOT NULL constraint residential_checks_task_answer_pk PRIMARY KEY,
    address_check_request_id INTEGER NOT NULL,
    task_code VARCHAR(64) NOT NULL,
    task_version VARCHAR(16) NOT NULL,
    answer_type VARCHAR(64) NOT NULL,
    answers jsonb NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    constraint fk_task_answer_check_request_id foreign key (address_check_request_id) references curfew_address_check_request(id)
);

CREATE INDEX idx_checks_task_answer_request_id ON residential_checks_task_answer(address_check_request_id);
