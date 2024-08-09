CREATE TABLE assessment
(
    id SERIAL NOT NULL constraint assessment_pk PRIMARY KEY,
    booking_id integer NOT NULL,
    prisoner_number varchar(7) NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
