insert into offender(id, booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, status)
values
    (1, 10, 'A1234AA', 'BMI', 'FIRST', 'LAST', '1981-03-12','2020-10-25', 'NOT_STARTED');

insert into assessment(id, offender_id,
                       status,
                       policy_version)
values (1,
        1,
        'AWAITING_ADDRESS_AND_RISK_CHECKS',
        '1.0');
