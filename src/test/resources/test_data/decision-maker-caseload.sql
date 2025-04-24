insert into staff (id, staff_code, forename, surname, username, email, kind)
values (1, 'STAFF1', 'a', 'com', 'a-com', 'a-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into staff (id, staff_code, forename, surname, username, email, kind)
values (2, 'STAFF2', 'another', 'com', 'another-com', 'another-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into offender(prison_number, prison_id, forename, surname, date_of_birth, crd, crn)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20','2020-11-14','DX12340A'),
       ( 'A1234AB', 'BMI', 'FIRST-2', 'LAST-2', '1983-06-03', '2020-11-14', 'DX12340B'),
       ( 'G9524ZF', 'EDF', 'FIRST-3', 'LAST-3', '1989-11-03', '2027-12-25', 'DX12340C'),
       ( 'A1234AD', 'BMI', 'FIRST-4', 'LAST-4', '2001-12-25', '2022-03-21', null),
       ( 'C1234CC', 'MSD', 'FIRST-5', 'LAST-5', '1964-02-21', '2020-11-14', 'DX12340E'),
       ( 'C1234CD', 'BMI', 'FIRST-6', 'LAST-6', '1987-04-18', '2023-06-01', 'DX12340F'),
       ( 'B1234BB', 'ABC', 'FIRST-7', 'LAST-7', '1969-05-15', '2021-12-18', 'DX12340G');

insert into assessment(booking_id,offender_id, status, policy_version, responsible_com_id, postponement_date, hdced)
values (10,1, 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', '1.0', 2, null, '2020-10-25'),
       (20,2, 'AWAITING_REFUSAL', '1.0', 1, '2021-12-18', '2020-10-25'),
       (30,3, 'APPROVED', '1.0', 1, null, '2020-10-25'),
       (40,4, 'TIMED_OUT', '1.0', 2, null, '2020-10-25'),
       (50,5, 'NOT_STARTED', '1.0', 1, null, '2020-10-25'),
       (60,6, 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', '1.0', 2, null, '2020-10-25'),
       (70,7, 'AWAITING_DECISION', '1.0', 1, null, '2020-10-25');

INSERT INTO public.postponement_reason
(assessment_id, reason_type)
VALUES(2, 'PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER'),
    (2, 'ON_REMAND'),
    (2, 'SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE'),
    (2, 'NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION'),
    (2, 'BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON'),
    (2, 'WOULD_NOT_FOLLOW_REQUIREMENTS_OF_CONFISCATION_ORDER'),
    (2, 'PENDING_APPLICATION_WITH_UNDULY_LENIENT_SENTENCE_SCH');