CREATE TABLE resident
(
    id SERIAL NOT NULL CONSTRAINT resident_pk PRIMARY KEY,
    forename VARCHAR(35),
    surname VARCHAR(35),
    phone_number VARCHAR(40),
    relation VARCHAR(50),
    date_of_birth DATE,
    age INTEGER,
    is_main_resident BOOLEAN NOT NULL ,
    standard_address_check_request_id INTEGER NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_resident_address_check FOREIGN KEY (standard_address_check_request_id) REFERENCES standard_address_check_request(id)
);
