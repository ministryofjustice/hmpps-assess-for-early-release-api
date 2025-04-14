ALTER table assessment
ADD COLUMN has_non_disclosable_information BOOLEAN,
ADD COLUMN non_disclosable_information VARCHAR(255);
