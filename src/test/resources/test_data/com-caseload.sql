insert into staff (id, staff_code, forename, surname, username, email, kind)
values (1, 'STAFF1', 'a', 'com', 'a-com', 'a-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into staff (id, staff_code, forename, surname, username, email, kind)
values (2, 'STAFF2', 'another', 'com', 'another-com', 'another-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into offender(booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status, crn)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED', 'DX12340A'),
       (20, 'A1234AB', 'BMI', 'FIRST-2', 'LAST-2', '1983-06-03', '2020-10-25', '2020-11-14', 'NOT_STARTED', null),
       (30, 'G9524ZF', 'EDF', 'FIRST-3', 'LAST-3', '1989-11-03', '2020-10-25', '2027-12-25', 'NOT_STARTED', 'DX12340C'),
       (40, 'A1234AD', 'BMI', 'FIRST-4', 'LAST-4', '2001-12-25', '2020-10-25', '2022-03-21', 'NOT_STARTED', 'DX12340D'),
       (50, 'C1234CC', 'MSD', 'FIRST-5', 'LAST-5', '1964-02-21', '2020-10-25', '2020-11-14', 'NOT_STARTED', 'DX12340E'),
       (60, 'C1234CD', 'BMI', 'FIRST-6', 'LAST-6', '1987-04-18', '2020-10-25', '2023-06-01', 'NOT_STARTED', 'DX12340F'),
       (70, 'B1234BB', 'ABC', 'FIRST-7', 'LAST-7', '1969-05-15', '2020-10-25', '2021-12-18', 'NOT_STARTED', 'DX12340G');

insert into assessment(offender_id, status, policy_version, responsible_com_id, postponement_date)
values ((select id from offender where booking_id = 10), 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', '1.0', 2, null),
       ((select id from offender where booking_id = 20), 'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS', '1.0', 1, '2021-12-18'),
       ((select id from offender where booking_id = 30), 'AWAITING_ADDRESS_AND_RISK_CHECKS', '1.0', 1, null),
       ((select id from offender where booking_id = 40), 'AWAITING_ADDRESS_AND_RISK_CHECKS', '1.0', 2,null),
       ((select id from offender where booking_id = 50), 'NOT_STARTED', '1.0', 1, null),
       ((select id from offender where booking_id = 60), 'AWAITING_DECISION', '1.0', 2, null),
       ((select id from offender where booking_id = 70), 'AWAITING_DECISION', '1.0', 1, null);

INSERT INTO public.postponement_reason
(assessment_id, reason_type)
VALUES((select id from offender where booking_id = 20), 'PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER'),
      ((select id from offender where booking_id = 20), 'ON_REMAND'),
      ((select id from offender where booking_id = 20), 'SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE'),
      ((select id from offender where booking_id = 20), 'NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION'),
      ((select id from offender where booking_id = 20), 'COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY'),
      ((select id from offender where booking_id = 20), 'CONFISCATION_ORDER_NOT_PAID_AND_ENF_AGENCY_DEEMS_UNSUITABLE'),
      ((select id from offender where booking_id = 20), 'PENDING_APPLICATION_WITH_UNDULY_LENIENT_LENIENT_SCH');