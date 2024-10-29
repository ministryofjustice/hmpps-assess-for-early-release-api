package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateEligibilityStatus

@Service
class AssessmentLifecycleService {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun eligibilityAnswerSubmitted(assessment: AssessmentWithEligibilityProgress): AssessmentStatus {
    return when (assessment.calculateAggregateEligibilityStatus()) {
      ELIGIBLE ->
        assessment.findStatusBefore(
          INELIGIBLE_OR_UNSUITABLE,
          ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
          NOT_STARTED,
        ) ?: ELIGIBLE_AND_SUITABLE

      INELIGIBLE -> INELIGIBLE_OR_UNSUITABLE
      IN_PROGRESS -> ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
      else -> error("Assessment: ${assessment.assessmentEntity.id} is in an unexpected state: ${assessment.assessmentEntity.status}")
    }
  }

  fun submitAssessmentForAddressChecks(assessment: AssessmentWithEligibilityProgress): AssessmentStatus =
    when (assessment.assessmentEntity.status.allowsTransitionTo(AWAITING_ADDRESS_AND_RISK_CHECKS)) {
      true -> AWAITING_ADDRESS_AND_RISK_CHECKS
      else -> error("Cannot submit Assessment: ${assessment.assessmentEntity.id}, in status: ${assessment.assessmentEntity.status}")
    }

  fun optOut(assessment: AssessmentWithEligibilityProgress): AssessmentStatus =
    when (assessment.assessmentEntity.status.allowsTransitionTo(OPTED_OUT)) {
      true -> OPTED_OUT
      else -> error("Cannot opt out an assessment: ${assessment.assessmentEntity.id} in status: ${assessment.assessmentEntity.status}`")
    }

  fun optBackIn(assessment: AssessmentWithEligibilityProgress): AssessmentStatus =
    if (assessment.assessmentEntity.status == OPTED_OUT) {
      assessment.findStatusBefore(OPTED_OUT)
        ?: error("Cannot find state for assessment: ${assessment.assessmentEntity.id} before it was opted out")
    } else {
      error("Cannot opt in for assessment: ${assessment.assessmentEntity.id} that was not opted out")
    }

  private fun AssessmentWithEligibilityProgress.findStatusBefore(vararg statuses: AssessmentStatus): AssessmentStatus? {
    val statusesToCheck = setOf(*statuses)
    val transitionBefore = this.assessmentEntity.assessmentEvents
      .reversed()
      .filterIsInstance<StatusChangedEvent>()
      .firstOrNull { !statusesToCheck.contains(it.changes.before) }
    return transitionBefore?.changes?.before
  }
}
