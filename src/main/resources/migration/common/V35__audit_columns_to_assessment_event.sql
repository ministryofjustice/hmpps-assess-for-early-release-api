ALTER TABLE assessment_event ADD COLUMN username VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE assessment_event ADD COLUMN role VARCHAR(100) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE assessment_event ADD COLUMN on_behalf_of VARCHAR(100);

ALTER TABLE assessment_event ADD CONSTRAINT assessment_event_role_chk CHECK (role in ('PRISON_CA', 'PRISON_DM', 'PROBATION_COM', 'SUPPORT', 'SYSTEM'));
