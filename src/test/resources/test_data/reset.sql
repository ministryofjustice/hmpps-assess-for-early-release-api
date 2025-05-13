
delete from resident;
TRUNCATE TABLE resident RESTART IDENTITY CASCADE;
delete from postponement_reason;
delete from residential_checks_task_answer;
delete from standard_address_check_request;
delete from cas_accommodation_assessment;
delete from curfew_address_check_request;
delete from address;
delete from eligibility_check_result;
delete from assessment_to_last_update_event;
delete from address_deletion_event;
delete from assessment_event;
delete from assessment;
delete from offender;
delete from staff;

-- Reset auto id in all tables with auto id
ALTER SEQUENCE resident_id_seq RESTART WITH 1;
ALTER SEQUENCE residential_checks_task_answer_id_seq RESTART WITH 1;
ALTER SEQUENCE curfew_address_check_request_id_seq RESTART WITH 1;
ALTER SEQUENCE address_id_seq RESTART WITH 1;
ALTER SEQUENCE eligibility_check_result_id_seq RESTART WITH 1;
ALTER SEQUENCE assessment_id_seq RESTART WITH 1;
ALTER SEQUENCE offender_id_seq RESTART WITH 1;
ALTER SEQUENCE staff_id_seq RESTART WITH 1;
ALTER SEQUENCE assessment_event_id_seq RESTART WITH 1;
