ALTER TABLE assessment ALTER COLUMN status TYPE varchar(64);


ALTER TABLE assessment DROP CONSTRAINT assessment_status_chk;
ALTER TABLE assessment
    ADD CONSTRAINT assessment_status_chk CHECK ( status IN (
                                                            'NOT_STARTED',
                                                            'ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS',
                                                            'ELIGIBLE_AND_SUITABLE',
                                                            'AWAITING_ADDRESS_AND_RISK_CHECKS',
                                                            'ADDRESS_AND_RISK_CHECKS_IN_PROGRESS',
                                                            'AWAITING_PRE_DECISION_CHECKS',
                                                            'AWAITING_DECISION',
                                                            'APPROVED',
                                                            'AWAITING_PRE_RELEASE_CHECKS',
                                                            'PASSED_PRE_RELEASE_CHECKS',
                                                            'ADDRESS_UNSUITABLE',
                                                            'AWAITING_REFUSAL',
                                                            'INELIGIBLE_OR_UNSUITABLE',
                                                            'REFUSED',
                                                            'TIMED_OUT',
                                                            'POSTPONED',
                                                            'OPTED_OUT',
                                                            'RELEASED_ON_HDC'
        ));
