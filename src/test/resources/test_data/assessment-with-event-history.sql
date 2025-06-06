insert into offender(prison_number, prison_id, forename, surname, date_of_birth, crn)
values ('A1234AA', 'AKI', 'FIRST-7', 'LAST-7', '1969-05-15', 'DX12340G');

insert into assessment(booking_id, offender_id, status, policy_version, opt_out_reason_type, opt_out_reason_other, postponement_date, created_timestamp, hdced, crd)
values (70,1, 'NOT_STARTED', '1.0','OTHER','I have reason',null, '2021-12-06', current_date + 7, '2021-12-18');

INSERT INTO public.assessment_event
(assessment_id, event_type, summary, event_time, username, role, full_name, on_behalf_of, changes)
VALUES(1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-01 17:19:08.993',
     'a-prison-user-old','PRISON_CA', 'Gara Toral','HEI', '{"after": "ELIGIBLE_AND_SUITABLE", "before": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb),
      (1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-02 17:19:08.993',
       'a-dm-user-old','PRISON_DM', 'Shijqa Kahnrah','HEI', '{"after": "ELIGIBLE_AND_SUITABLE", "before": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb),
      (1, 'RESIDENT_UPDATED', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-02 17:19:08.993',
       'a-probation-user-old-1','PROBATION_COM', 'Lursa Torghn','HEI', '{"after": "ELIGIBLE_AND_SUITABLE", "before": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb),
      (1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-02 17:19:08.993',
       'a-probation-user-old-2','PROBATION_COM', 'Kali Hurgas','HEI', '{"after": "ELIGIBLE_AND_SUITABLE", "before": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb),
     (1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-09 17:19:08.993',
       'a-prison-user','PRISON_CA', 'Bura Hurn','HEI', '{"after": "ELIGIBLE_AND_SUITABLE", "before": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb),
      (1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-10 17:19:08.993',
       'a-dm-user','PRISON_DM', 'Kreg Rahnaz','HEI', '{"after": "ELIGIBLE_AND_SUITABLE", "before": "ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb),
      (1, 'STATUS_CHANGE', 'status changed from: ''NOT_STARTED'', to: ''ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS''', '2024-10-11 17:19:08.993',
       'a-probation-user','PROBATION_COM', 'Margon Antaak','BRI', '{"after": "NOT_STARTED", "before": "ELIGIBLE_AND_SUITABLE", "context": {"code": "terrorist-offending-history", "type": "suitability", "answers": {"terroristOffendingHistory": false}}}'::jsonb);

INSERT INTO public.postponement_reason
(assessment_id, reason_type)
VALUES(1, 'PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER'),
      (1, 'ON_REMAND'),
      (1, 'SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE'),
      (1, 'NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION'),
      (1, 'BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON'),
      (1, 'WOULD_NOT_FOLLOW_REQUIREMENTS_OF_CONFISCATION_ORDER'),
      (1, 'PENDING_APPLICATION_WITH_UNDULY_LENIENT_SENTENCE_SCH');
