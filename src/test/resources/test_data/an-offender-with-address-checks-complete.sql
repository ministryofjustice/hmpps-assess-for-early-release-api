insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(booking_id, offender_id, status, address_checks_complete, policy_version, has_non_disclosable_information, non_disclosable_information)
values (10, 1, 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', 'TRUE', '1.0', false, null);
