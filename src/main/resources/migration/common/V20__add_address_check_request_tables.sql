CREATE TABLE curfew_address_check_request
(
    id SERIAL NOT NULL constraint curfew_address_check_request_pk PRIMARY KEY,
    ca_additional_info TEXT,
    pp_additional_info TEXT,
    date_requested DATE NOT NULL,
    preference_priority VARCHAR(20) NOT NULL CONSTRAINT curfew_address_check_request_priority_chk CHECK(preference_priority in ('FIRST', 'SECOND', 'THIRD', 'FOURTH')),
    status VARCHAR(20) NOT NULL CONSTRAINT curfew_address_check_request_status_chk CHECK(status in ('IN_PROGRESS', 'UNSUITABLE', 'SUITABLE')),
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE standard_address_check_request
(
    id INTEGER NOT NULL CONSTRAINT standard_address_check_request_pk PRIMARY KEY,
    address_id INTEGER NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_standard_curfew_check FOREIGN KEY (id) REFERENCES curfew_address_check_request(id),
    CONSTRAINT fk_standard_address_check_address FOREIGN KEY (address_id) REFERENCES address(id)
);

CREATE TABLE cas_check_request
(
    id INTEGER NOT NULL CONSTRAINT cas_check_request_pk PRIMARY KEY,
    allocated_address_id INTEGER,
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_cas_curfew_check FOREIGN KEY (id) REFERENCES curfew_address_check_request(id),
    CONSTRAINT fk_cas_request_check_address FOREIGN KEY (allocated_address_id) REFERENCES address(id)
);
