package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner

@Component
class OffenderToAssessmentSummaryMapper(
  private val prisonService: PrisonService,
) {

  fun map(currentAssessment: Assessment): AssessmentSummary {
    val offender = currentAssessment.offender
    val prisonerSearchResults = getPrisonerDetails(offender)
    val prisonName = prisonService.getPrisonNameForId(offender.prisonId)

    val prisonerDetails = prisonerSearchResults.first()
    return AssessmentSummary(
      bookingId = currentAssessment.bookingId,
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = prisonName,
      status = currentAssessment.status,
      responsibleCom = currentAssessment.responsibleCom?.toSummary(),
      teamCode = currentAssessment.teamCode,
      policyVersion = currentAssessment.policyVersion,
      optOutReasonType = currentAssessment.optOutReasonType,
      optOutReasonOther = currentAssessment.optOutReasonOther,
      postponementReasons = currentAssessment.postponementReasons.map { reason -> reason.reasonType }.toList(),
      cellLocation = prisonerDetails.cellLocation,
      mainOffense = prisonerDetails.mostSeriousOffence,
      tasks = currentAssessment.status.tasks().mapValues { (_, tasks) ->
        tasks.map { TaskProgress(it.task, it.status(currentAssessment)) }
      },
      lastUpdateBy = currentAssessment.lastUpdateByUserEvent?.agent?.fullName,
    )
  }

  private fun getPrisonerDetails(offender: Offender): List<PrisonerSearchPrisoner> {
    val prisonerSearchResults = prisonService.searchPrisonersByNomisIds(listOf(offender.prisonNumber))
    if (prisonerSearchResults.isEmpty()) {
      throw ItemNotFoundException("Could not find prisoner details for ${offender.prisonNumber}")
    }
    return prisonerSearchResults
  }
}
