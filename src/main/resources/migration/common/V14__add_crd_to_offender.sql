ALTER TABLE offender ADD COLUMN crd DATE;

CREATE INDEX idx_offender_prisoner_number ON offender(prisoner_number);
