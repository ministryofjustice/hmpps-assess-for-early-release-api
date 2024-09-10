ALTER TABLE assessment DROP CONSTRAINT assessment_status_chk;
ALTER TABLE assessment ADD CONSTRAINT assessment_status_chk CHECK ( status IN ('NOT_STARTED', 'OPTED_OUT'));
