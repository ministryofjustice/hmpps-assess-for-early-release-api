package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.calculateAggregateStatus

@Service
class AssessmentLifecycleService {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun eligibilityAnswerSubmitted(assessment: AssessmentWithEligibilityProgress): AssessmentStatus {
    return when (assessment.calculateAggregateStatus()) {
      ELIGIBLE ->
        // Find the previous event that occurred before eligibility checks took place
        assessment.assessmentEntity.assessmentEvents
          .reversed()
          .filterIsInstance<StatusChangedEvent>()
          .firstOrNull {
            !setOf(
              INELIGIBLE_OR_UNSUITABLE,
              ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
              NOT_STARTED,
            ).contains(it.changes.before)
          }?.changes?.before ?: AssessmentStatus.ELIGIBLE_AND_SUITABLE

      INELIGIBLE -> INELIGIBLE_OR_UNSUITABLE
      IN_PROGRESS -> ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
      else -> error("Assessment: ${assessment.assessmentEntity.id} is in an unexpected state: ${assessment.assessmentEntity.status}")
    }
  }

  fun submitAssessmentForAddressChecks(assessment: AssessmentWithEligibilityProgress): AssessmentStatus = when (assessment.calculateAggregateStatus()) {
    ELIGIBLE -> AWAITING_ADDRESS_AND_RISK_CHECKS
    else -> error("Cannot submit an assessment that is not eligible and suitable")
  }
}
