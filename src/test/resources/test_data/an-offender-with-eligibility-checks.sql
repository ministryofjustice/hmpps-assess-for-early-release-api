insert into offender(booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(offender_id, status, policy_version)
values ((select id from offender where booking_id = 10), 'NOT_STARTED', '1.0');

insert into criteria_check
(assessment_id, criteria_code, criteria_version, criteria_met, criteria_type, question_answers)
VALUES((select max(id) from assessment a), 'sex-offender-register', '1.0', true, 'ELIGIBILITY', '{"requiredToSignSexOffenderRegister": true}'::jsonb);

insert into criteria_check
(assessment_id, criteria_code, criteria_version, criteria_met, criteria_type, question_answers)
VALUES((select max(id) from assessment a), 'category-a', '1.0', true, 'SUITABILITY', '{"categoryA": true}'::jsonb);
