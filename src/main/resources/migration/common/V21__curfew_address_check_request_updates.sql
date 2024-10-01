ALTER TABLE curfew_address_check_request ALTER COLUMN date_requested TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE curfew_address_check_request ALTER COLUMN date_requested SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE curfew_address_check_request ALTER COLUMN date_requested SET NOT NULL;
ALTER TABLE curfew_address_check_request ADD COLUMN assessment_id INTEGER;
ALTER TABLE curfew_address_check_request ADD CONSTRAINT fk_curfew_address_check_assessment foreign key (assessment_id) references assessment(id);
