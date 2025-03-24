insert into staff (id, staff_code, forename, surname, username, email, kind)
values (1, 'STAFF1', 'a', 'com', 'a-com', 'a-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into staff (id, staff_code, forename, surname, username, email, kind)
values (2, 'STAFF2', 'another', 'com', 'another-com', 'another-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status, crn)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED', 'DX12340A'),
       ( 'A1234AB', 'BMI', 'FIRST-2', 'LAST-2', '1983-06-03', '2020-10-25', '2020-11-14', 'NOT_STARTED', 'DX12340B'),
       ( 'G9524ZF', 'EDF', 'FIRST-3', 'LAST-3', '1989-11-03', '2020-10-25', '2027-12-25', 'NOT_STARTED', 'DX12340C'),
       ( 'A1234AD', 'BMI', 'FIRST-4', 'LAST-4', '2001-12-25', '2020-10-25', '2022-03-21', 'NOT_STARTED', null),
       ( 'C1234CC', 'MSD', 'FIRST-5', 'LAST-5', '1964-02-21', '2020-10-25', '2020-11-14', 'NOT_STARTED', 'DX12340E'),
       ( 'C1234CD', 'BMI', 'FIRST-6', 'LAST-6', '1987-04-18', '2020-10-25', '2023-06-01', 'NOT_STARTED', 'DX12340F'),
       ( 'B1234BB', 'ABC', 'FIRST-7', 'LAST-7', '1969-05-15', '2020-10-25', '2021-12-18', 'NOT_STARTED', 'DX12340G');

insert into assessment(booking_id,offender_id, status, policy_version, responsible_com_id, postponement_date)
values (10,1, 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', '1.0', 2, null),
       (20,2, 'AWAITING_REFUSAL', '1.0', 1, '2021-12-18'),
       (30,3, 'APPROVED', '1.0', 1, null),
       (40,4, 'TIMED_OUT', '1.0', 2, null),
       (50,5, 'NOT_STARTED', '1.0', 1, null),
       (60,6, 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', '1.0', 2, null),
       (70,7, 'AWAITING_DECISION', '1.0', 1, null);

INSERT INTO public.postponement_reason
(assessment_id, reason_type)
VALUES(2, 'PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER'),
    (2, 'ON_REMAND'),
    (2, 'SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE'),
    (2, 'NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION'),
    (2, 'COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY'),
    (2, 'CONFISCATION_ORDER_NOT_PAID_AND_ENF_AGENCY_DEEMS_UNSUITABLE'),
    (2, 'PENDING_APPLICATION_WITH_UNDULY_LENIENT_LENIENT_SCH');