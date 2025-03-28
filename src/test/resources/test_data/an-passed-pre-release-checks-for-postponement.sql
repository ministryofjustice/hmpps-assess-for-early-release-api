insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', '2020-10-25 ', '2020-11-14', 'NOT_STARTED');

insert into assessment(booking_id,offender_id, status, policy_version)
values (10,1, 'PASSED_PRE_RELEASE_CHECKS', '1.0');

INSERT INTO public.assessment_event(assessment_id, event_type, summary, event_time, changes)
VALUES(1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-09 17:19:08.993', '{"after": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "before": "NOT_STARTED"}'::jsonb);