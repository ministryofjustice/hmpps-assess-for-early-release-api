package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.toStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate

const val DAYS_TO_ADD = 5L

@Component
class AssessmentToAssessmentOverviewSummaryMapper {

  fun map(assessmentWithEligibilityProgress: AssessmentWithEligibilityProgress, prisonName: String, prisonerSearchResults: PrisonerSearchPrisoner): AssessmentOverviewSummary {
    val offender = assessmentWithEligibilityProgress.offender
    val currentAssessment = assessmentWithEligibilityProgress.assessmentEntity
    val eligibility = assessmentWithEligibilityProgress.getEligibilityProgress()
    val eligibilityStatus = eligibility.toStatus()
    val suitability = assessmentWithEligibilityProgress.getSuitabilityProgress()
    val suitabilityStatus = suitability.toStatus()
    return AssessmentOverviewSummary(
      bookingId = currentAssessment.bookingId,
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = currentAssessment.hdced,
      crd = currentAssessment.crd,
      location = prisonName,
      addressChecksStatus = currentAssessment.addressChecksStatus,
      status = currentAssessment.status,
      responsibleCom = currentAssessment.responsibleCom?.toSummary(),
      teamCode = currentAssessment.teamCode,
      policyVersion = currentAssessment.policyVersion,
      optOutReasonType = currentAssessment.optOutReasonType,
      optOutReasonOther = currentAssessment.optOutReasonOther,
      cellLocation = prisonerSearchResults.cellLocation,
      mainOffense = prisonerSearchResults.mostSeriousOffence,
      tasks = currentAssessment.tasks(),
      toDoEligibilityAndSuitabilityBy = getToDoByDate(offender),
      result = determineResult(eligibilityStatus, suitabilityStatus),
      hasNonDisclosableInformation = currentAssessment.hasNonDisclosableInformation,
      nonDisclosableInformation = currentAssessment.nonDisclosableInformation,
      victimContactSchemeOptedIn = currentAssessment.victimContactSchemeOptedIn,
      victimContactSchemeRequests = currentAssessment.victimContactSchemeRequests,
      pomBehaviourInformation = currentAssessment.pomBehaviourInformation,
      lastUpdateBy = currentAssessment.lastUpdateByUserEvent?.agent?.fullName,
    )
  }

  private fun getToDoByDate(offender: Offender): LocalDate {
    val createdDate = offender.createdTimestamp.toLocalDate()
    return createdDate.plusDays(DAYS_TO_ADD)
  }

  private fun determineResult(eligibilityStatus: EligibilityStatus, suitabilityStatus: SuitabilityStatus): String? = when {
    eligibilityStatus == INELIGIBLE && suitabilityStatus == SUITABLE -> "Ineligible"
    eligibilityStatus == ELIGIBLE && suitabilityStatus == UNSUITABLE -> "Unsuitable"
    eligibilityStatus == INELIGIBLE && suitabilityStatus == UNSUITABLE -> "Ineligible and Unsuitable"
    eligibilityStatus == ELIGIBLE && suitabilityStatus == SUITABLE -> "Eligible and Suitable"
    else -> null
  }
}
