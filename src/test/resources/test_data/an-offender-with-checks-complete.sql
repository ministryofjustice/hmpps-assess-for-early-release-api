insert into offender(booking_id, prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values (10, 'A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(offender_id, status, policy_version)
values ((select id from offender where booking_id = 10), 'NOT_STARTED', '1.0');

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'sex-offender-register', '1.0', true, 'ELIGIBILITY', '{"requiredToSignSexOffenderRegister": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'extended-sentence-for-violent-or-sexual-offences', '1.0', true, 'ELIGIBILITY', '{"servingExtendedSentenceForViolentOrSexualOffence": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'rotl-failure-to-return', '1.0', true, 'ELIGIBILITY', '{"rotlFailedToReturn": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'community-order-curfew-breach', '1.0', true, 'ELIGIBILITY', '{"communityOrderCurfewBreach": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'recalled-for-breaching-hdc-curfew', '1.0', true, 'ELIGIBILITY', '{"recalledForBreachingHdcCurfew": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'deportation-orders', '1.0', true, 'ELIGIBILITY', '{"recommendedForDeportation": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'recalled-from-early-release-on-compassionate-grounds', '1.0', true, 'ELIGIBILITY', '{"recalledFromEarlyReleaseOnCompassionateGrounds": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'terrorism', '1.0', true, 'ELIGIBILITY', '{"terroristSentenceOrOffending": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'two-thirds-release', '1.0', true, 'ELIGIBILITY', '{"subjectToTwoThirdsReleaseCriteria": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'section-244ZB-notice', '1.0', true, 'ELIGIBILITY', '{"served244ZBNoticeInForce": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'section-20b-release', '1.0', true, 'ELIGIBILITY', '{"schedule20BRelease": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'sexual-offending-history', '1.0', true, 'SUITABILITY', '{"historyOfSexualOffending": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'liable-to-deportation', '1.0', true, 'SUITABILITY', '{"liableToDeportation": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'recalled-for-poor-behaviour-on-hdc', '1.0', true, 'SUITABILITY', '{"poorBehaviourOnHdc": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'category-a', '1.0', true, 'SUITABILITY', '{"categoryA": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'rosh-and-mappa', '1.0', true, 'SUITABILITY', '{"highOrVeryHighRoSH": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'presumed-unsuitable-offences', '1.0', true, 'SUITABILITY', '{"presumedUnsuitableOffences": false}'::jsonb);

insert into eligibility_check_result
(assessment_id, criterion_code, criterion_version, criterion_met, criterion_type, question_answers)
VALUES((select max(id) from assessment a), 'terrorist-offending-history', '1.0', true, 'SUITABILITY', '{"terroristOffendingHistory": false}'::jsonb);
