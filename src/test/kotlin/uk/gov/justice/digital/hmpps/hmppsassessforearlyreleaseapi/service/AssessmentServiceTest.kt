package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffenderWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService
import java.time.LocalDate

class AssessmentServiceTest {
  private val prisonRegisterService = mock<PrisonRegisterService>()
  private val offenderRepository = mock<OffenderRepository>()
  private val assessmentRepository = mock<AssessmentRepository>()

  private val service =
    AssessmentService(PolicyService(), prisonRegisterService, offenderRepository, assessmentRepository)

  @Test
  fun `should get an offenders current assessment`() {
    val hdced = LocalDate.now().plusDays(5)
    val offender = anOffender(hdced)
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
    whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    val assessment = service.getCurrentAssessmentSummary(PRISON_NUMBER)

    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    verify(prisonRegisterService).getNameForId(PRISON_ID)
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
    val anOffender = anOffenderWithSomeProgress()
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)
//    whenever(assessmentLifecycleService.optOut(anyOrNull())).thenReturn(OPTED_OUT)

    service.optOut(PRISON_NUMBER)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(OPTED_OUT)
  }

  @Test
  fun `should opt-in an offender`() {
    val anOffender = anOffenderWithSomeProgress()
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
    whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)
//    whenever(assessmentLifecycleService.optBackIn(anyOrNull())).thenReturn(AWAITING_ADDRESS_AND_RISK_CHECKS)

    service.optBackIn(PRISON_NUMBER)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should submit an assessment`() {
    val assessment = anAssessmentWithEligibilityProgress()

    val anOffender = anOffender()
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender.copy())
    whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)
//    whenever(assessmentLifecycleService.submitAssessmentForAddressChecks(anyOrNull())).thenReturn(AWAITING_ADDRESS_AND_RISK_CHECKS)

//    whenever(assessmentLifecycleService.submitAssessmentForAddressChecks(assessment)).thenReturn(
//      AWAITING_ADDRESS_AND_RISK_CHECKS,
//    )

    service.submitAssessmentForAddressChecks(PRISON_NUMBER)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value)
      .extracting("status")
      .isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Nested
  inner class GetCurrentAssessment {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffender()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender.copy())
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val currentAssessment = service.getCurrentAssessment(PRISON_NUMBER)

      assertThat(currentAssessment.assessmentEntity).isEqualTo(anOffender.currentAssessment())

      val eligibilityProgress = currentAssessment.eligibilityProgress()
      assertThat(eligibilityProgress).hasSize(11)
      assertThat(eligibilityProgress).allMatch { it.status == NOT_STARTED }

      val suitabilityProgress = currentAssessment.suitabilityProgress()
      assertThat(suitabilityProgress).hasSize(7)
      assertThat(suitabilityProgress).allMatch { it.status == SuitabilityStatus.NOT_STARTED }
    }

    @Test
    fun `for an offender with some progress`() {
      val offender = anOffenderWithSomeProgress()

      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val currentAssessment = service.getCurrentAssessment(PRISON_NUMBER)

      val eligibilityProgress = currentAssessment.eligibilityProgress()
      assertThat(eligibilityProgress).hasSize(11)
      assertThat(eligibilityProgress).filteredOn { it.status == NOT_STARTED }.hasSize(10)
      assertThat(eligibilityProgress).filteredOn { it.status == ELIGIBLE }.hasSize(1)

      val suitabilityProgress = currentAssessment.suitabilityProgress()
      assertThat(suitabilityProgress).hasSize(7)
      assertThat(suitabilityProgress).filteredOn { it.status == SuitabilityStatus.NOT_STARTED }.hasSize(6)
      assertThat(suitabilityProgress).filteredOn { it.status == SuitabilityStatus.SUITABLE }.hasSize(1)
    }
  }

  @Nested
  inner class GetSuitabilityCriterionView {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffenderWithSomeProgress()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val criterion1 = POLICY_1_0.suitabilityCriteria[0]

      val assessment = service.getCurrentAssessment(PRISON_NUMBER)

      assertThat(assessment.suitabilityProgress().find { it.code === criterion1.code }).isEqualTo(
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
        ),
      )
    }
  }

  @Nested
  inner class GetEligibilityCriterionView {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffenderWithSomeProgress()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val criterion1 = POLICY_1_0.eligibilityCriteria[0]

      val assessment = service.getCurrentAssessment(PRISON_NUMBER)

      assertThat(assessment.eligibilityProgress().find { it.code === criterion1.code }).isEqualTo(
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
        ),
      )
    }
  }
}
