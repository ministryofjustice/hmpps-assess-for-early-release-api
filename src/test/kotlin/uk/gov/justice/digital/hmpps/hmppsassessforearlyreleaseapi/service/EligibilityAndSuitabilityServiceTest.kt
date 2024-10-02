package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriteriaType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService

class EligibilityAndSuitabilityServiceTest {
  private val prisonRegisterService = mock<PrisonRegisterService>()
  private val assessmentRepository = mock<AssessmentRepository>()
  private val offenderRepository = mock<OffenderRepository>()

  private val service =
    EligibilityAndSuitabilityService(PolicyService(), prisonRegisterService, assessmentRepository, offenderRepository)

  @Nested
  inner class GetCaseView {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffender()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender.copy())
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val caseView = service.getCaseView(PRISON_NUMBER)

      assertThat(caseView.assessmentSummary).isEqualTo(
        anAssessmentSummary().copy(
          hdced = anOffender.hdced,
          crd = anOffender.crd,
        ),
      )

      assertThat(caseView.eligibility).hasSize(11)
      assertThat(caseView.eligibility).allMatch { it.status == NOT_STARTED }

      assertThat(caseView.suitability).hasSize(7)
      assertThat(caseView.suitability).allMatch { it.status == SuitabilityStatus.NOT_STARTED }
    }

    @Test
    fun `for an offender with some progress`() {
      val offender = anOffenderWithSomeProgress()

      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val caseView = service.getCaseView(PRISON_NUMBER)

      assertThat(caseView.assessmentSummary).isEqualTo(
        anAssessmentSummary().copy(
          hdced = offender.hdced,
          crd = offender.crd,
        ),
      )

      assertThat(caseView.eligibility).hasSize(11)
      assertThat(caseView.eligibility).filteredOn { it.status == NOT_STARTED }.hasSize(10)
      assertThat(caseView.eligibility).filteredOn { it.status == ELIGIBLE }.hasSize(1)

      assertThat(caseView.suitability).hasSize(7)
      assertThat(caseView.suitability).filteredOn { it.status == SuitabilityStatus.NOT_STARTED }.hasSize(6)
      assertThat(caseView.suitability).filteredOn { it.status == SuitabilityStatus.SUITABLE }.hasSize(1)
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
      val criterion2 = POLICY_1_0.suitabilityCriteria[1]

      val criterionView = service.getSuitabilityCriterionView(PRISON_NUMBER, criterion1.code)

      assertThat(criterionView.assessmentSummary).isEqualTo(
        anAssessmentSummary().copy(
          hdced = anOffender.hdced,
          crd = anOffender.crd,
        ),
      )

      assertThat(criterionView.criterion).isEqualTo(
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
      assertThat(criterionView.nextCriterion).isEqualTo(
        SuitabilityCriterionProgress(
          code = criterion2.code,
          taskName = criterion2.name,
          status = SuitabilityStatus.NOT_STARTED,
          questions = criterion2.questions.map {
            Question(
              text = it.text,
              hint = it.hint,
              name = it.name,
              answer = null,
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
      val criterion2 = POLICY_1_0.eligibilityCriteria[1]

      val criterionView = service.getEligibilityCriterionView(PRISON_NUMBER, criterion1.code)

      assertThat(criterionView.assessmentSummary).isEqualTo(
        anAssessmentSummary().copy(
          hdced = anOffender.hdced,
          crd = anOffender.crd,
        ),
      )

      assertThat(criterionView.criterion).isEqualTo(
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
      assertThat(criterionView.nextCriterion).isEqualTo(
        EligibilityCriterionProgress(
          code = criterion2.code,
          taskName = criterion2.name,
          status = NOT_STARTED,
          questions = criterion2.questions.map {
            Question(
              text = it.text,
              hint = it.hint,
              name = it.name,
              answer = null,
            )
          },
        ),
      )
    }
  }

  @Nested
  inner class SaveAnswer {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffender()
      whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(anOffender)
      whenever(prisonRegisterService.getNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

      val criterion = POLICY_1_0.eligibilityCriteria[0]
      val question = criterion.questions.first()

      val criterionChecks = CriterionCheck(
        code = criterion.code,
        type = CriteriaType.ELIGIBILITY,
        answers = mapOf(question.name to true),
      )

      assertThat(anOffender.currentAssessment().criteriaCheck).isEmpty()

      service.saveAnswer(PRISON_NUMBER, criterionChecks)

      argumentCaptor<Assessment> {
        verify(assessmentRepository).save(capture())

        assertThat(firstValue.criteriaCheck).hasSize(1)
        with(firstValue.criteriaCheck.first()) {
          assertThat(criteriaCode).isEqualTo(criterion.code)
          assertThat(criteriaType).isEqualTo(ELIGIBILITY)
          assertThat(criteriaMet).isEqualTo(true)
          assertThat(criteriaVersion).isEqualTo(firstValue.policyVersion)
          assertThat(assessment).isEqualTo(firstValue)
          assertThat(questionAnswers).isEqualTo(mapOf(question.name to true))
        }
      }
    }
  }

  private fun anOffenderWithSomeProgress() =
    anOffender().let {
      val currentAssessment = it.currentAssessment()
      val eligibilityCriteria = POLICY_1_0.eligibilityCriteria.first()
      val suitabilityCriteria = POLICY_1_0.suitabilityCriteria.first()
      val assessment = currentAssessment.copy(
        criteriaCheck = mutableSetOf(
          CriteriaCheck(
            assessment = currentAssessment,
            criteriaCode = eligibilityCriteria.code,
            criteriaType = ELIGIBILITY,
            criteriaMet = true,
            id = 1,
            criteriaVersion = POLICY_1_0.code,
            questionAnswers = mapOf(eligibilityCriteria.questions.first().name to true),
          ),
          CriteriaCheck(
            assessment = currentAssessment,
            criteriaCode = suitabilityCriteria.code,
            criteriaType = SUITABILITY,
            criteriaMet = true,
            id = 1,
            criteriaVersion = POLICY_1_0.code,
            questionAnswers = mapOf(suitabilityCriteria.questions.first().name to true),
          ),
        ),
      )
      it.copy(assessments = mutableSetOf(assessment))
    }
}
