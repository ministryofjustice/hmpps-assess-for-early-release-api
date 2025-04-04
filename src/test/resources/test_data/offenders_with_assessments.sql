insert into offender(prison_number, prison_id, forename, surname, date_of_birth, hdced, crd, status, crn)
values ('A1234AA', 'BMI', 'FIRST-1', 'LAST-1', '1978-03-20', current_date + 7, '2020-11-14', 'NOT_STARTED', 'DX12340A');

insert into staff (id, staff_code, forename, surname, username, email, kind)
values (1, 'STAFF2', 'another', 'com', 'another-com', 'another-com@justice.gov.uk', 'COMMUNITY_OFFENDER_MANAGER');

insert into assessment(booking_id, offender_id, status, responsible_com_id,policy_version, opt_out_reason_type, opt_out_reason_other, postponement_date, created_timestamp,deleted_timestamp)
values (10,1, 'AWAITING_ADDRESS_AND_RISK_CHECKS', 1, '1.0','OTHER','I have reason','2021-12-18', '2021-12-06','2021-12-06'),
       (20,1, 'AWAITING_ADDRESS_AND_RISK_CHECKS', 1,'1.0', null, null,null, '2021-12-06', null);
