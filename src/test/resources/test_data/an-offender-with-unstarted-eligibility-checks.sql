insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(booking_id, offender_id, status, policy_version)
values (10,1, 'NOT_STARTED', '1.0');

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES (1, 'sex-offender-register', '1.0', true, 'ELIGIBILITY',
        '{"requiredToSignSexOffenderRegister": false}'::jsonb);
