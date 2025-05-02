insert into offender(prison_number,
                     prison_id,
                     forename,
                     surname,
                     date_of_birth,
                     hdced,
                     crd,
                     status)
values ('A1234AA',
        'BMI',
        'forename',
        'surname',
        '1978-03-20',
        '2020-10-25',
        '2020-11-14',
        'NOT_STARTED');

insert into assessment(booking_id,
                       offender_id,
                       status,
                       policy_version)
values (10,
        1,
        'AWAITING_ADDRESS_AND_RISK_CHECKS',
        '1.0');

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
insert into residential_checks_task_answer(id, address_check_request_id, task_code, task_version, criterion_met)
values (1, 1, 'address-details-and-informed-consent', 'V1', true),
       (2, 1, 'assess-this-persons-risk', 'V1', true),
       (3, 1, 'children-services-check', 'V1', true),
       (4, 1, 'police-check', 'V1', true),
       (5, 1, 'make-a-risk-management-decision', 'V1', true),
       (6, 1, 'suitability-decision', 'V1', true);
