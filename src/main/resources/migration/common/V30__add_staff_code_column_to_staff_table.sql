ALTER TABLE staff ADD COLUMN staff_code VARCHAR(10) NOT NULL;
ALTER TABLE staff DROP COLUMN staff_identifier;
