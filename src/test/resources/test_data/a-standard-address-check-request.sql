insert into offender(prison_number,
                     prison_id,
                     forename,
                     surname,
                     date_of_birth)
values ('A1234AA',
        'BMI',
        'forename',
        'surname',
        '1978-03-20');

insert into assessment(booking_id,
                       offender_id,
                       status,
                       policy_version,
                       hdced,
                       crd)
values (10,
        1,
        'AWAITING_ADDRESS_AND_RISK_CHECKS',
        '1.0',
        '2020-10-25',
        '2020-11-14');

insert into address (uprn,
                     first_line,
                     second_line,
                     town,
                     county,
                     postcode,
                     country,
                     x_coordinate,
                     y_coordinate,
                     address_last_updated)
values ('200010019924',
        '4 ADANAC DRIVE',
        'NURSLING',
        'SOUTHAMPTON',
        'TEST VALLEY',
        'SO16 0AS',
        'England',
        437292.43,
        115541.95,
        '2020-03-31');


insert into curfew_address_check_request(preference_priority, status, assessment_id)
values ('FIRST', 'IN_PROGRESS', 1);
insert into standard_address_check_request(id,address_id)
values (1,1);
