package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary

@Component
class AssessmentToAssessmentResponseMapper {

  fun map(assessment: Assessment): AssessmentResponse = AssessmentResponse(
    id = assessment.id,
    bookingId = assessment.bookingId,
    status = assessment.status,
    previousStatus = assessment.previousStatus,
    createdTimestamp = assessment.createdTimestamp,
    lastUpdatedTimestamp = assessment.lastUpdatedTimestamp,
    deletedTimestamp = assessment.deletedTimestamp,
    policyVersion = assessment.policyVersion,
    addressChecksComplete = assessment.addressChecksComplete,
    responsibleCom = assessment.responsibleCom?.toSummary(),
    team = assessment.teamCode,
    postponementDate = assessment.postponementDate,
    optOutReasonType = assessment.optOutReasonType,
    optOutReasonOther = assessment.optOutReasonOther,
  )
}
