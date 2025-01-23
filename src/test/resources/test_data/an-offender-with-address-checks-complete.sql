insert into offender(booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(offender_id, status, address_checks_complete, policy_version)
values ((select id from offender where booking_id = 10), 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', 'TRUE', '1.0');
