insert into offender(booking_id, prison_number, prison_id, forename, surname, hdced, crd, status)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '2020-10-25', '2020-11-14', 'NOT_STARTED'),
       (20, 'A1234AB', 'BMI', 'FIRST-2', 'LAST-2', '2020-10-25', '2020-11-14', 'NOT_STARTED'),
       (30, 'A1234AC', 'EDF', 'FIRST-3', 'LAST-3', '2020-10-25', '2027-12-25', 'NOT_STARTED'),
       (40, 'A1234AD', 'BMI', 'FIRST-4', 'LAST-4', '2020-10-25', '2022-03-21', 'NOT_STARTED'),
       (50, 'C1234CC', 'MSD', 'FIRST-5', 'LAST-5', '2020-10-25', '2020-11-14', 'NOT_STARTED'),
       (60, 'C1234CD', 'BMI', 'FIRST-6', 'LAST-6', '2020-10-25', '2023-06-01', 'NOT_STARTED'),
       (70, 'B1234BB', 'ABC', 'FIRST-7', 'LAST-7', '2020-10-25', '2021-12-18', 'NOT_STARTED');

insert into assessment(offender_id, status)
values ((select id from offender where booking_id = 10), 'NOT_STARTED'),
       ((select id from offender where booking_id = 20), 'NOT_STARTED'),
       ((select id from offender where booking_id = 30), 'NOT_STARTED'),
       ((select id from offender where booking_id = 40), 'NOT_STARTED'),
       ((select id from offender where booking_id = 50), 'NOT_STARTED'),
       ((select id from offender where booking_id = 60), 'NOT_STARTED'),
       ((select id from offender where booking_id = 70), 'NOT_STARTED')