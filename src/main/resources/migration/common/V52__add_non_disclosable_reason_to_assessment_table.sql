ALTER table assessment
ADD COLUMN is_non_disclosable BOOLEAN,
ADD COLUMN non_disclosable_reason VARCHAR(255);
