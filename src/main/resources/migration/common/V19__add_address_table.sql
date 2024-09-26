CREATE TABLE address
(
    id SERIAL NOT NULL constraint address_pk PRIMARY KEY,
    uprn varchar(12) NOT NULL,
    first_line varchar(50),
    second_line varchar(35),
    town varchar(30) NOT NULL,
    county varchar(50),
    postcode varchar(8) NOT NULL,
    country varchar(100) NOT NULL,
    x_coordinate real NOT NULL,
    y_coordinate real NOT NULL,
    address_last_updated DATE NOT NULL ,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
