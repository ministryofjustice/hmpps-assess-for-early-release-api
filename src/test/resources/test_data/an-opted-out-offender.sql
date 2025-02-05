insert into offender(booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(offender_id, status, previous_status, policy_version)
values ((select id from offender where booking_id = 10), 'OPTED_OUT', 'ELIGIBLE_AND_SUITABLE', '1.0');
