ALTER TABLE assessment ADD COLUMN hdced DATE;
ALTER TABLE assessment ADD COLUMN crd DATE;
ALTER TABLE assessment ADD COLUMN sentence_start_date DATE;

UPDATE assessment
SET
    hdced = offender.hdced,
    crd = offender.crd,
    sentence_start_date = offender.sentence_start_date
FROM offender
WHERE assessment.offender_id = offender.id;

ALTER TABLE assessment ALTER COLUMN hdced SET NOT NULL;

ALTER TABLE offender DROP COLUMN hdced;
ALTER TABLE offender DROP COLUMN crd;
ALTER TABLE offender DROP COLUMN sentence_start_date;
