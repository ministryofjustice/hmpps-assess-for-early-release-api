insert into offender(prison_number, prison_id, forename, surname, date_of_birth)
values
    ( 'A1234AA', 'BMI', 'FIRST', 'LAST', '1981-03-12'),
    ( 'A1234AB', 'BMI', 'FIRST', 'LAST','1981-03-12'),
    ('A1234AC', 'EDF', 'FIRST', 'LAST','1981-03-12'),
    ('A1234AD', 'BMI', 'FIRST', 'LAST','1981-03-12'),
    ('C1234CC', 'MSD', 'FIRST', 'LAST','1981-03-12'),
    ('C1234CD', 'BMI', 'FIRST', 'LAST','1981-03-12'),
    ('B1234BB', 'ABC', 'FIRST', 'LAST','1981-03-12');

insert into assessment(booking_id,
                       offender_id,
                       status,
                       policy_version,
                       hdced)
values (10,
        1,
        'AWAITING_ADDRESS_AND_RISK_CHECKS',
        '1.0',
        '2025-10-25');

