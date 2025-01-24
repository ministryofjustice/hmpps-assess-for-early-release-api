insert into offender(id,
                     booking_id,
                     prison_number,
                     prison_id,
                     forename,
                     surname,
                     date_of_birth,
                     hdced,
                     crd,
                     status)
values (1,
        10,
        'A1234AA',
        'BMI',
        'forename',
        'surname',
        '1978-03-20',
        '2020-10-25',
        '2020-11-14',
        'NOT_STARTED');

insert into assessment(id, offender_id,
                       status,
                       policy_version)
values (1,
        (select id from offender where booking_id = 10),
        'AWAITING_ADDRESS_AND_RISK_CHECKS',
        '1.0');

insert into address (id,
                     uprn,
                     first_line,
                     second_line,
                     town,
                     county,
                     postcode,
                     country,
                     x_coordinate,
                     y_coordinate,
                     address_last_updated)
values (1,
        '200010019924',
        '4 ADANAC DRIVE',
        'NURSLING',
        'SOUTHAMPTON',
        'TEST VALLEY',
        'SO16 0AS',
        'England',
        437292.43,
        115541.95,
        '2020-03-31');


insert into curfew_address_check_request(id, preference_priority, status, assessment_id)
values (1, 'FIRST', 'IN_PROGRESS', 1);
insert into standard_address_check_request(id, address_id)
values (1, 1);
