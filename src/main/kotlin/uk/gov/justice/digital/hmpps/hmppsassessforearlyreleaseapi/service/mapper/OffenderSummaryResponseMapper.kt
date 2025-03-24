package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.CaseloadService.Companion.DAYS_BEFORE_SENTENCE_START

@Component
class OffenderSummaryResponseMapper {

  fun map(assessment: Assessment, workingDaysToHdced: Int): OffenderSummaryResponse {
    val offender = assessment.offender

    return OffenderSummaryResponse(
      prisonNumber = offender.prisonNumber,
      bookingId = assessment.bookingId,
      forename = offender.forename!!,
      surname = offender.surname!!,
      hdced = offender.hdced,
      workingDaysToHdced = workingDaysToHdced,
      probationPractitioner = assessment.responsibleCom?.fullName,
      isPostponed = assessment.status == AssessmentStatus.POSTPONED,
      postponementDate = assessment.postponementDate,
      postponementReasons = assessment.postponementReasons.map { reason -> reason.reasonType }.toList(),
      status = assessment.status,
      addressChecksComplete = assessment.addressChecksComplete,
      currentTask = assessment.currentTask(),
      taskOverdueOn = offender.sentenceStartDate?.plusDays(DAYS_BEFORE_SENTENCE_START),
      crn = offender.crn,
    )
  }
}
