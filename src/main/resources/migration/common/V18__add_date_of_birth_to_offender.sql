ALTER TABLE offender ADD COLUMN date_of_birth DATE;
UPDATE offender set date_of_birth = current_date;
ALTER TABLE offender ALTER COLUMN date_of_birth SET NOT NULL;
