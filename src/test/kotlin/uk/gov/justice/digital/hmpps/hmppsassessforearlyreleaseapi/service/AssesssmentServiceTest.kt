package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffenderWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService

class AssesssmentServiceTest {
  private val prisonRegisterService = mock<PrisonRegisterService>()
  private val offenderRepository = mock<OffenderRepository>()

  private val service =
    AssessmentService(PolicyService(), prisonRegisterService, offenderRepository)

  @Nested
  inner class GetCurrentAssessment {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffender()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender.copy())
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val currentAssessment = service.getCurrentAssessment(PRISON_NUMBER)

      assertThat(currentAssessment.assessmentEntity).isEqualTo(anOffender.currentAssessment())

      assertThat(currentAssessment.eligibilityProgress).hasSize(11)
      assertThat(currentAssessment.eligibilityProgress).allMatch { it.status == NOT_STARTED }

      assertThat(currentAssessment.suitabilityProgress).hasSize(7)
      assertThat(currentAssessment.suitabilityProgress).allMatch { it.status == SuitabilityStatus.NOT_STARTED }
    }

    @Test
    fun `for an offender with some progress`() {
      val offender = anOffenderWithSomeProgress()

      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val currentAssessment = service.getCurrentAssessment(PRISON_NUMBER)

      assertThat(currentAssessment.eligibilityProgress).hasSize(11)
      assertThat(currentAssessment.eligibilityProgress).filteredOn { it.status == NOT_STARTED }.hasSize(10)
      assertThat(currentAssessment.eligibilityProgress).filteredOn { it.status == ELIGIBLE }.hasSize(1)

      assertThat(currentAssessment.suitabilityProgress).hasSize(7)
      assertThat(currentAssessment.suitabilityProgress).filteredOn { it.status == SuitabilityStatus.NOT_STARTED }
        .hasSize(6)
      assertThat(currentAssessment.suitabilityProgress).filteredOn { it.status == SuitabilityStatus.SUITABLE }
        .hasSize(1)
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

      assertThat(assessment.suitabilityProgress.find { it.code === criterion1.code }).isEqualTo(
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

      assertThat(assessment.eligibilityProgress.find { it.code === criterion1.code }).isEqualTo(
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
