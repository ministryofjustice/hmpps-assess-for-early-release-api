-- Create link table
CREATE TABLE address_to_assessments (
        check_request_id int8 NOT NULL,
        assessment_id int8 NOT NULL,
        CONSTRAINT address_to_assessments_pkey PRIMARY KEY (check_request_id, assessment_id)
);

-- rename table to standard naming used for derived tables
ALTER TABLE cas_check_request RENAME TO cas_address_check_request;

-- Migrate data across
INSERT INTO address_to_assessments (check_request_id, assessment_id)
     SELECT id as check_request_id,
            assessment_id
     FROM curfew_address_check_request
        WHERE assessment_id is not null;
-- delete old column
ALTER TABLE curfew_address_check_request DROP COLUMN assessment_id;
