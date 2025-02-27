ALTER TABLE eligibility_check_result ADD COLUMN username VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE eligibility_check_result ADD COLUMN full_name VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE eligibility_check_result ADD COLUMN role VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE eligibility_check_result ADD COLUMN on_behalf_of VARCHAR(100);

ALTER TABLE eligibility_check_result ADD CONSTRAINT eligibility_check_result_role_chk CHECK (role in ('PRISON_CA', 'PRISON_DM', 'PROBATION_COM', 'SUPPORT', 'SYSTEM'));

ALTER TABLE assessment_event ADD COLUMN full_name VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';