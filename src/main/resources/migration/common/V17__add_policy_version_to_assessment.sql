ALTER TABLE assessment ADD COLUMN policy_version varchar(8);
UPDATE assessment set policy_version = '1.0';
ALTER TABLE assessment ALTER COLUMN policy_version SET NOT NULL;


