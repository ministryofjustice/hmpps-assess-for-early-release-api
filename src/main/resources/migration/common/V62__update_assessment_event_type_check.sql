ALTER TABLE assessment_event
    DROP CONSTRAINT assessment_event_event_type_chk,
    ADD CONSTRAINT assessment_event_event_type_chk CHECK ( event_type IN ('STATUS_CHANGE',
                                                                          'RESIDENT_UPDATED',
                                                                          'ADDRESS_UPDATED',
                                                                          'ADDRESS_DELETED',
                                                                          'RESIDENTIAL_CHECKS_TASK_ANSWERS_UPDATED',
                                                                          'PRISON_TRANSFERRED',
                                                                          'PRISONER_UPDATED',
                                                                          'PRISONER_CREATED',
                                                                          'ASSESSMENT_DELETED',
                                                                          'NONDISCLOSURE_INFORMATION_ENTRY',
                                                                          'VLO_AND_POM_CONSULTATION_UPDATED'));
