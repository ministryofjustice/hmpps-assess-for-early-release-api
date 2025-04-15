ALTER TABLE assessment_event DROP CONSTRAINT assessment_event_event_type_chk;
ALTER TABLE assessment_event
    ADD CONSTRAINT assessment_event_event_type_chk CHECK ( event_type IN ('STATUS_CHANGE',
                                                                          'RESIDENT_UPDATED',
                                                                          'ADDRESS_UPDATED',
                                                                          'RESIDENTIAL_CHECKS_TASK_ANSWERS_UPDATED',
                                                                          'PRISON_TRANSFERRED',
                                                                          'PRISONER_UPDATED',
                                                                          'PRISONER_CREATED',
                                                                          'ASSESSMENT_DELETED',
                                                                          'NONDISCLOSURE_INFORMATION_ENTRY'));
