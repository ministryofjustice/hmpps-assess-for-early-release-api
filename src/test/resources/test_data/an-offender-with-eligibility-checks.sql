insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(booking_id, offender_id, status, policy_version, eligibility_checks_status)
values (10,1, 'NOT_STARTED', '1.0', 'IN_PROGRESS');

INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of,
 created_timestamp, last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'sex-offender-register', '1.0', true, 'ELIGIBILITY',
        'wayneroberts', 'Wayne Roberts', 'PRISON_CA', 'MDI',
        '2024-11-06 06:37:35.801', '2024-11-06 06:37:35.801', '{"requiredToSignSexOffenderRegister": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of,
 created_timestamp, last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'extended-sentence-for-violent-or-sexual-offences', '1.0', true,
        'ELIGIBILITY', 'wayneroberts', 'Wayne Roberts', 'PRISON_CA', 'MDI',
        '2024-11-06 06:37:43.943', '2024-11-06 06:37:43.943',
        '{"servingExtendedSentenceForViolentOrSexualOffence": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'rotl-failure-to-return', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI',
        '2024-11-06 06:37:45.150', '2024-11-06 06:37:45.150', '{"rotlFailedToReturn": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'community-order-curfew-breach', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI', '2024-11-06 06:37:46.171', '2024-11-06 06:37:46.171',
        '{"communityOrderCurfewBreach": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'recalled-for-breaching-hdc-curfew', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI',
        '2024-11-06 06:37:47.561', '2024-11-06 06:37:47.561', '{"recalledForBreachingHdcCurfew": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'deportation-orders', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI', '2024-11-06 06:37:50.439',
        '2024-11-06 06:37:50.439', '{"servedADecisionToDeport": false, "recommendedForDeportation": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'recalled-from-early-release-on-compassionate-grounds', '1.0', true,
        'ELIGIBILITY','helenreid', 'Helen Reid', 'PRISON_CA', 'MDI',
        '2024-11-06 06:37:52.417', '2024-11-06 06:37:52.417',
        '{"recalledFromEarlyReleaseOnCompassionateGrounds": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'terrorism', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI', '2024-11-06 06:37:53.590',
        '2024-11-06 06:37:53.590', '{"terroristSentenceOrOffending": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'two-thirds-release', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI', '2024-11-06 06:37:57.613',
        '2024-11-06 06:37:57.613',
        '{"subjectToTwoThirdsReleaseCriteria": false, "retrospectiveTwoThirdsReleaseCriteria": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'section-244ZB-notice', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI',
        '2024-11-06 06:37:59.720', '2024-11-06 06:37:59.720', '{"served244ZBNoticeInForce": false}'::jsonb);
INSERT INTO public.eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, username, full_name, role, on_behalf_of, created_timestamp,
 last_updated_timestamp, question_answers)
VALUES ((select max(id) from assessment a), 'section-20b-release', '1.0', true, 'ELIGIBILITY',
        'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI',
        '2024-11-06 06:38:00.766', '2024-11-06 06:38:00.766', '{"schedule20BRelease": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers, username, full_name, role, on_behalf_of, last_updated_timestamp)
VALUES ((select max(id) from assessment a), 'category-a', '1.0', true, 'SUITABILITY',
        '{"categoryA": false}'::jsonb, 'helenreid', 'Helen Reid', 'PRISON_CA', 'MDI', '2024-11-06 06:38:00.766');
