insert into offender(booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', current_date + 7, '2020-11-14', 'NOT_STARTED'),
       (20, 'A1234AB', 'BMI', 'FIRST-2', 'LAST-2', '1983-06-03', current_date + 7, '2020-11-14', 'NOT_STARTED'),
       (30, 'A1234AC', 'EDF', 'FIRST-3', 'LAST-3', '1989-11-03', current_date + 7, '2027-12-25', 'NOT_STARTED'),
       (40, 'A1234AD', 'BMI', 'FIRST-4', 'LAST-4', '2001-12-25', current_date + 7, '2022-03-21', 'NOT_STARTED'),
       (50, 'C1234CC', 'MSD', 'FIRST-5', 'LAST-5', '1964-02-21', current_date + 7, '2020-11-14', 'NOT_STARTED'),
       (60, 'C1234CD', 'BMI', 'FIRST-6', 'LAST-6', '1987-04-18', current_date + 7, '2023-06-01', 'NOT_STARTED'),
       (70, 'B1234BB', 'ABC', 'FIRST-7', 'LAST-7', '1969-05-15', current_date + 7, '2021-12-18', 'NOT_STARTED');

insert into assessment(offender_id, status, policy_version)
values ((select id from offender where booking_id = 10), 'NOT_STARTED', '1.0'),
       ((select id from offender where booking_id = 20), 'NOT_STARTED', '1.0'),
       ((select id from offender where booking_id = 30), 'NOT_STARTED', '1.0'),
       ((select id from offender where booking_id = 40), 'NOT_STARTED', '1.0'),
       ((select id from offender where booking_id = 50), 'NOT_STARTED', '1.0'),
       ((select id from offender where booking_id = 60), 'NOT_STARTED', '1.0'),
       ((select id from offender where booking_id = 70), 'NOT_STARTED', '1.0')