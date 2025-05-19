ALTER TABLE curfew_address_check_request ADD COLUMN address_deletion_event_id bigint null;
ALTER TABLE curfew_address_check_request
    ADD CONSTRAINT fk_curfew_address_check_request_address_deletion_event
        FOREIGN KEY (address_deletion_event_id) references address_deletion_event(id);
