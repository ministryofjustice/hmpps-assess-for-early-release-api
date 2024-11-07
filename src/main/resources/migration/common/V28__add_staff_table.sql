CREATE TABLE staff
(
    id SERIAL NOT NULL CONSTRAINT staff_pk PRIMARY KEY,
    staff_identifier BIGINT NOT NULL,
    username VARCHAR(40) CONSTRAINT staff_username_key UNIQUE,
    email VARCHAR(200),
    forename VARCHAR(60) NOT NULL,
    surname VARCHAR(60) NOT NULL,
    kind VARCHAR(30) NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER table assessment ADD COLUMN responsible_com_id INTEGER;
ALTER TABLE assessment ADD CONSTRAINT fk_assessment_staff foreign key (responsible_com_id) references staff(id);
