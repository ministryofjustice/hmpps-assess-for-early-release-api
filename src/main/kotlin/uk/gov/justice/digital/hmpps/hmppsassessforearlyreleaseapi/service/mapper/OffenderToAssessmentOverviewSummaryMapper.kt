package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PolicyService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.toStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate

const val DAYS_TO_ADD = 5L

@Component
class OffenderToAssessmentOverviewSummaryMapper(
  private val prisonService: PrisonService,
  private val policyService: PolicyService,
) {

  fun map(offender: Offender): AssessmentOverviewSummary {
    val prisonerSearchResults = getPrisonerDetails(offender)
    val currentAssessment = offender.currentAssessment()
    val prisonName = prisonService.getPrisonNameForId(offender.prisonId)

    val assessmentWithEligibilityProgress = getCurrentAssessmentWithEligibilityProgress(offender)

    val eligibility = assessmentWithEligibilityProgress.getEligibilityProgress()
    val eligibilityStatus = eligibility.toStatus()
    val suitability = assessmentWithEligibilityProgress.getSuitabilityProgress()
    val suitabilityStatus = suitability.toStatus()

    val prisonerDetails = prisonerSearchResults.first()
    return AssessmentOverviewSummary(
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = prisonName,
      status = currentAssessment.status,
      responsibleCom = currentAssessment.responsibleCom?.toSummary(),
      team = currentAssessment.team,
      policyVersion = currentAssessment.policyVersion,
      optOutReasonType = currentAssessment.optOutReasonType,
      optOutReasonOther = currentAssessment.optOutReasonOther,
      cellLocation = prisonerDetails.cellLocation,
      mainOffense = prisonerDetails.mostSeriousOffence,
      tasks = currentAssessment.status.tasks().mapValues { (_, tasks) ->
        tasks.map { TaskProgress(it.task, it.status(currentAssessment)) }
      },
      toDoEligibilityAndSuitabilityBy = getToDoByDate(offender),
      result = determineResult(eligibilityStatus, suitabilityStatus),
    )
  }

  private fun getToDoByDate(offender: Offender): LocalDate {
    val createdDate = offender.createdTimestamp.toLocalDate()
    return createdDate.plusDays(DAYS_TO_ADD)
  }

  private fun getPrisonerDetails(offender: Offender): List<PrisonerSearchPrisoner> {
    val prisonerSearchResults = prisonService.searchPrisonersByNomisIds(listOf(offender.prisonNumber))
    if (prisonerSearchResults.isEmpty()) {
      throw ItemNotFoundException("Could not find prisoner details for ${offender.prisonNumber}")
    }
    return prisonerSearchResults
  }

  private fun getCurrentAssessmentWithEligibilityProgress(offender: Offender): AssessmentWithEligibilityProgress {
    val currentAssessment = offender.currentAssessment()
    val policy = policyService.getVersionFromPolicy(currentAssessment.policyVersion)
    return AssessmentWithEligibilityProgress(
      assessmentEntity = currentAssessment,
      policy = policy,
    )
  }

  private fun determineResult(eligibilityStatus: EligibilityStatus, suitabilityStatus: SuitabilityStatus): String? {
    return when {
      eligibilityStatus == INELIGIBLE && suitabilityStatus == NOT_STARTED -> "Ineligible"
      eligibilityStatus == ELIGIBLE && suitabilityStatus == UNSUITABLE -> "Unsuitable"
      eligibilityStatus == INELIGIBLE && suitabilityStatus == UNSUITABLE -> "Ineligible and Unsuitable"
      eligibilityStatus == ELIGIBLE && suitabilityStatus == SUITABLE -> "Eligible and Suitable"
      else -> null
    }
  }
}
