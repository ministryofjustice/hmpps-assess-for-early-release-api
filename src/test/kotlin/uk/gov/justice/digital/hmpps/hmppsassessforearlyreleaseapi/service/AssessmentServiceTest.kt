package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ResultType.PASSED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithCompleteEligibilityChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.saveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import java.time.LocalDate

class AssessmentServiceTest {
  private val prisonService = mock<PrisonService>()
  private val offenderRepository = mock<OffenderRepository>()
  private val assessmentRepository = mock<AssessmentRepository>()

  private val offenderToAssessmentSummaryMapper = OffenderToAssessmentSummaryMapper(prisonService)

  private val service =
    AssessmentService(PolicyService(), offenderRepository, assessmentRepository, offenderToAssessmentSummaryMapper)

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
    ).isEqualTo(listOf(FORENAME, SURNAME, PRISON_NUMBER, hdced, null, PRISON_NAME, AssessmentStatus.NOT_STARTED))
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

  @Nested
  inner class GetCurrentAssessment {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffender()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender.copy())
      whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val currentAssessment = service.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)

      assertThat(currentAssessment.assessmentEntity).isEqualTo(anOffender.currentAssessment())

      val eligibilityProgress = currentAssessment.getEligibilityProgress()
      assertThat(eligibilityProgress).hasSize(11)
      assertThat(eligibilityProgress).allMatch { it.status == NOT_STARTED }

      val suitabilityProgress = currentAssessment.getSuitabilityProgress()
      assertThat(suitabilityProgress).hasSize(7)
      assertThat(suitabilityProgress).allMatch { it.status == SuitabilityStatus.NOT_STARTED }
    }

    @Test
    fun `for an offender with some progress`() {
      val assessment = anAssessmentWithSomeProgress(
        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.specifyByIndex(0 to PASSED),
        suitabilityProgress = Progress.specifyByIndex(0 to PASSED),
      )

      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(assessment.offender)
      whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val currentAssessment = service.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)

      val eligibilityProgress = currentAssessment.getEligibilityProgress()
      assertThat(eligibilityProgress).hasSize(11)
      assertThat(eligibilityProgress).filteredOn { it.status == NOT_STARTED }.hasSize(10)
      assertThat(eligibilityProgress).filteredOn { it.status == ELIGIBLE }.hasSize(1)

      val suitabilityProgress = currentAssessment.getSuitabilityProgress()
      assertThat(suitabilityProgress).hasSize(7)
      assertThat(suitabilityProgress).filteredOn { it.status == SuitabilityStatus.NOT_STARTED }.hasSize(6)
      assertThat(suitabilityProgress).filteredOn { it.status == SuitabilityStatus.SUITABLE }.hasSize(1)
    }
  }

  @Nested
  inner class GetSuitabilityCriterionView {
    @Test
    fun `for existing unstarted offender`() {
      val offender = anAssessmentWithSomeProgress(
        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        suitabilityProgress = Progress.specifyByIndex(0 to PASSED),
      ).offender

      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
      whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val criterion1 = POLICY_1_0.suitabilityCriteria[0]

      val assessment = service.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)

      assertThat(assessment.getSuitabilityProgress().find { it.code === criterion1.code }).isEqualTo(
        SuitabilityCriterionProgress(
          code = criterion1.code,
          taskName = criterion1.name,
          status = SuitabilityStatus.SUITABLE,
          questions = criterion1.questions.map {
            Question(
              text = it.text,
              hint = it.hint,
              name = it.name,
              answer = true,
            )
          },
          agent = PRISON_CA_AGENT,
          lastUpdated = LocalDate.now(),
        ),
      )
    }
  }

  @Nested
  inner class GetEligibilityCriterionView {
    @Test
    fun `for existing unstarted offender`() {
      val offender = anAssessmentWithSomeProgress(
        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.specifyByIndex(0 to PASSED),
      ).offender

      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
      whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val criterion1 = POLICY_1_0.eligibilityCriteria[0]

      val assessment = service.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)

      assertThat(assessment.getEligibilityProgress().find { it.code === criterion1.code }).isEqualTo(
        EligibilityCriterionProgress(
          code = criterion1.code,
          taskName = criterion1.name,
          status = ELIGIBLE,
          questions = criterion1.questions.map {
            Question(
              text = it.text,
              hint = it.hint,
              name = it.name,
              answer = true,
            )
          },
          agent = PRISON_CA_AGENT,
          lastUpdated = LocalDate.now(),
        ),
      )
    }
  }
}
