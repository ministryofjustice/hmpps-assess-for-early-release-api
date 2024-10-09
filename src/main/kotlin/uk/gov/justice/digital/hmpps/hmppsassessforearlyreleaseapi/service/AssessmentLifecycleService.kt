package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.inProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.isChecksPassed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.isComplete

@Service
class AssessmentLifecycleService {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun eligibilityAnswerSubmitted(assessment: AssessmentWithEligibilityProgress): AssessmentStatus {
    return when {
      assessment.isChecksPassed() ->
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

      assessment.isComplete() -> INELIGIBLE_OR_UNSUITABLE
      assessment.inProgress() -> ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
      else -> error("Assessment: ${assessment.assessmentEntity.id} is in an unknown state")
    }
  }
}
