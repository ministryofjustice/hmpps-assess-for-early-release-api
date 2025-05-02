insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status, crn)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', current_date + 7, '2020-11-14', 'NOT_STARTED', 'DX12340A'),
       ('A1234AB', 'BMI', 'FIRST-2', 'LAST-2', '1983-06-03', current_date + 7, '2020-11-14', 'NOT_STARTED', null),
       ('A1234AC', 'EDF', 'FIRST-3', 'LAST-3', '1989-11-03', current_date + 7, '2027-12-25', 'NOT_STARTED', 'DX12340C'),
       ('A1234AD', 'BMI', 'FIRST-4', 'LAST-4', '2001-12-25', current_date + 7, '2022-03-21', 'NOT_STARTED', 'DX12340D'),
       ('C1234CC', 'MSD', 'FIRST-5', 'LAST-5', '1964-02-21', current_date + 7, '2020-11-14', 'NOT_STARTED', 'DX12340E'),
       ('C1234CD', 'BMI', 'FIRST-6', 'LAST-6', '1987-04-18', current_date + 7, '2023-06-01', 'NOT_STARTED', 'DX12340F'),
       ('B1234BB', 'ABC', 'FIRST-7', 'LAST-7', '1969-05-15', current_date + 7, '2021-12-18', 'NOT_STARTED', 'DX12340G');


insert into assessment(id, booking_id, offender_id, status, policy_version, opt_out_reason_type, opt_out_reason_other, postponement_date, created_timestamp)
values (1,10,1, 'NOT_STARTED', '1.0','OTHER','I have reason','2021-12-18', '2021-12-06'),
       (2,20,2, 'AWAITING_ADDRESS_AND_RISK_CHECKS', '1.0', null, null,null, '2021-12-06'),
       (3,30,3, 'NOT_STARTED', '1.0','OTHER','I have reason',null, '2021-12-06'),
       (4,40,4, 'AWAITING_PRE_DECISION_CHECKS', '1.0',null,null,null, '2021-12-06'),
       (5,50,5, 'AWAITING_PRE_RELEASE_CHECKS', '1.0',null,null,null, '2021-12-06'),
       (6,60,6, 'NOT_STARTED', '1.0','OTHER','I have reason',null, '2021-12-06'),
       (7,70,7, 'NOT_STARTED', '1.0','OTHER','I have reason',null, '2021-12-06');

INSERT INTO public.postponement_reason
(assessment_id, reason_type)
VALUES(1, 'PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER'),
      (1, 'ON_REMAND'),
      (1, 'SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE'),
      (1, 'NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION'),
      (1, 'BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON'),
      (1, 'WOULD_NOT_FOLLOW_REQUIREMENTS_OF_CONFISCATION_ORDER'),
      (1, 'PENDING_APPLICATION_WITH_UNDULY_LENIENT_SENTENCE_SCH');

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES(1, 'sex-offender-register', '1.0', false, 'ELIGIBILITY', '{"requiredToSignSexOffenderRegister": false}'::jsonb);
