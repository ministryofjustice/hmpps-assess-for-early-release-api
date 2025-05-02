insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, status)
values
    ( 'A1234AA', 'BMI', 'FIRST', 'LAST', '1981-03-12','2020-10-25', 'NOT_STARTED'),
    ( 'A1234AB', 'BMI', 'FIRST', 'LAST','1981-03-12','2020-10-25', 'NOT_STARTED'),
    ('A1234AC', 'EDF', 'FIRST', 'LAST','1981-03-12','2020-10-25', 'NOT_STARTED'),
    ('A1234AD', 'BMI', 'FIRST', 'LAST','1981-03-12','2020-10-25', 'NOT_STARTED'),
    ('C1234CC', 'MSD', 'FIRST', 'LAST','1981-03-12','2020-10-25', 'NOT_STARTED'),
    ('C1234CD', 'BMI', 'FIRST', 'LAST','1981-03-12','2020-10-25', 'NOT_STARTED'),
    ('B1234BB', 'ABC', 'FIRST', 'LAST','1981-03-12','2020-10-25', 'NOT_STARTED');

insert into assessment(booking_id,
                       offender_id,
                       status,
                       policy_version)
values (10,
        1,
        'AWAITING_ADDRESS_AND_RISK_CHECKS',
        '1.0');

