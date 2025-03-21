-- add column
ALTER table assessment ADD COLUMN booking_id integer;

-- Migrate data across
UPDATE assessment
    SET booking_id = offender.booking_id
        FROM offender
    WHERE assessment.offender_id = offender.id;

-- Now data has been migrated change to not null
ALTER TABLE assessment ALTER COLUMN booking_id SET NOT NULL;

-- Drop booking_id from offender
ALTER TABLE offender DROP COLUMN booking_id;
