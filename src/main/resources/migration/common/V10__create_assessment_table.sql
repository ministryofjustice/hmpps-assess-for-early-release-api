CREATE TABLE assessment
(
    id SERIAL NOT NULL constraint assessment_pk PRIMARY KEY,
    booking_id integer,
    noms_id varchar(7),
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
