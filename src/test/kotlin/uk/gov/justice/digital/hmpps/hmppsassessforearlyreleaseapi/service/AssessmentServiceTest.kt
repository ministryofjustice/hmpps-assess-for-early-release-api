package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithCompleteEligibilityChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.saveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentOverviewSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import java.time.LocalDate

class AssessmentServiceTest {
  private val prisonService = mock<PrisonService>()
  private val offenderRepository = mock<OffenderRepository>()
  private val assessmentRepository = mock<AssessmentRepository>()
  private val policyService = PolicyService()

  private val offenderToAssessmentSummaryMapper = OffenderToAssessmentSummaryMapper(prisonService)
  private val offenderToAssessmentOverviewSummaryMapper = OffenderToAssessmentOverviewSummaryMapper()

  private val service =
    AssessmentService(offenderRepository, assessmentRepository, offenderToAssessmentSummaryMapper, offenderToAssessmentOverviewSummaryMapper, prisonService, policyService)

  @Test
  fun `should get an offenders current assessment`() {
    // Given
    val hdced = LocalDate.now().plusDays(5)
    val offender = anOffender(hdced)
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
    whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

    // When
    val assessment = service.getCurrentAssessmentSummary(PRISON_NUMBER)

    // Then
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    assertThat(assessment).isNotNull
    assertThat(assessment).extracting(
      "forename",
      "surname",
      "prisonNumber",
      "hdced",
      "crd",
      "location",
      "status",
    ).isEqualTo(listOf(FORENAME, SURNAME, PRISON_NUMBER, hdced, null, PRISON_NAME, NOT_STARTED))
  }

  @Test
  fun `should get an offenders current assessment overview`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      ELIGIBLE_AND_SUITABLE,
      eligibilityProgress = Progress.allSuccessful(),
      suitabilityProgress = Progress.allSuccessful(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
    whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

    // When
    val assessment = service.getAssessmentOverviewSummary(PRISON_NUMBER)

    // Then
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    assertThat(assessment).isNotNull
    assertThat(assessment).extracting(
      "forename",
      "surname",
      "prisonNumber",
      "hdced",
      "crd",
      "location",
      "status",
      "toDoEligibilityAndSuitabilityBy",
      "result",
    ).isEqualTo(
      listOf(
        FORENAME, SURNAME, PRISON_NUMBER,
        LocalDate.now().plusDays(7), null, PRISON_NAME, ELIGIBLE_AND_SUITABLE, LocalDate.now().plusDays(5), "Eligible and Suitable",
      ),
    )
  }

  @Test
  fun `should throw exception when prisoner details not found`() {
    // Given
    val prisonNumber = "A1234AA"
    whenever(offenderRepository.findByPrisonNumber(prisonNumber)).thenReturn(anOffender())
    whenever(prisonService.searchPrisonersByNomisIds(listOf(prisonNumber))).thenReturn(emptyList())

    // When / Then
    val exception = org.junit.jupiter.api.assertThrows<ItemNotFoundException> {
      service.getAssessmentOverviewSummary(prisonNumber)
    }
    assertThat(exception.message).isEqualTo("Could not find prisoner details for $prisonNumber")
  }

  @Test
  fun `should opt-out an offender`() {
    val offender = anAssessmentWithCompleteEligibilityChecks(status = AWAITING_ADDRESS_AND_RISK_CHECKS).offender

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.optOut(PRISON_NUMBER, OptOutRequest(reasonType = NO_REASON_GIVEN, agent = PRISON_CA_AGENT))

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(OPTED_OUT)
    assertThat(assessmentCaptor.value.previousStatus).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should opt-in an offender`() {
    val assessment =
      anAssessmentWithCompleteEligibilityChecks(status = OPTED_OUT, previousStatus = AWAITING_ADDRESS_AND_RISK_CHECKS)

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(assessment.offender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.optBackIn(PRISON_NUMBER, PRISON_CA_AGENT)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should submit an assessment for address checks`() {
    val anOffender = anOffender()
    anOffender.currentAssessment().status = ELIGIBLE_AND_SUITABLE

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.submitAssessmentForAddressChecks(PRISON_NUMBER, PRISON_CA_AGENT)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should submit an assessment for pre-decision checks`() {
    val anOffender = anOffender()
    anOffender.currentAssessment().status = ADDRESS_AND_RISK_CHECKS_IN_PROGRESS

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.submitForPreDecisionChecks(PRISON_NUMBER, PROBATION_COM_AGENT)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_PRE_DECISION_CHECKS)
  }

  @Test
  fun `should update address checks status when checks complete`() {
    val anOffender = anOffender()
    anOffender.currentAssessment().status = ADDRESS_AND_RISK_CHECKS_IN_PROGRESS

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.updateAddressChecksStatus(PRISON_NUMBER, ResidentialChecksStatus.SUITABLE, saveResidentialChecksTaskAnswersRequest)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    assertThat(assessmentCaptor.value.addressChecksComplete).isTrue()
  }

  @Test
  fun `should not update address checks status when checks already complete`() {
    val anOffender = anOffender()
    anOffender.currentAssessment().status = ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
    anOffender.currentAssessment().addressChecksComplete = true

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.updateAddressChecksStatus(PRISON_NUMBER, ResidentialChecksStatus.SUITABLE, saveResidentialChecksTaskAnswersRequest)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    assertThat(assessmentCaptor.value.addressChecksComplete).isTrue()
  }

  @Test
  fun `should update address checks status to in progress`() {
    val anOffender = anOffender()
    anOffender.currentAssessment().status = AWAITING_ADDRESS_AND_RISK_CHECKS

    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.updateAddressChecksStatus(PRISON_NUMBER, ResidentialChecksStatus.IN_PROGRESS, saveResidentialChecksTaskAnswersRequest)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    assertThat(assessmentCaptor.value.addressChecksComplete).isFalse()
  }

  @Test
  fun `should update the team on assessments when their com is updated`() {
    val com = aCommunityOffenderManager()
    val newTeamCode = "N68ABC"
    val assessment = Assessment(
      offender = anOffender(),
      responsibleCom = com,
    )
    whenever(assessmentRepository.findByResponsibleComStaffCodeAndStatusIn(com.staffCode, AssessmentStatus.inFlightStatuses())).thenReturn(
      listOf(assessment),
    )

    service.updateTeamForResponsibleCom(com.staffCode, newTeamCode)

    val assessmentCaptor = argumentCaptor<List<Assessment>>()
    verify(assessmentRepository).findByResponsibleComStaffCodeAndStatusIn(com.staffCode, AssessmentStatus.inFlightStatuses())
    verify(assessmentRepository).saveAll(assessmentCaptor.capture())
    assessmentCaptor.firstValue.map { assertThat(it.team).isEqualTo(newTeamCode) }
  }
}
