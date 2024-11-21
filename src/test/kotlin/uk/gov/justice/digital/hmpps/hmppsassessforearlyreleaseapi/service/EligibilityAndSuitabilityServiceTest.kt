package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0

class EligibilityAndSuitabilityServiceTest {
  private val assessmentService = mock<AssessmentService>()
  private val assessmentRepository = mock<AssessmentRepository>()

  private val service =
    EligibilityAndSuitabilityService(
      PolicyService(),
      assessmentService,
      assessmentRepository,
    )

  @Nested
  inner class GetCaseView {
    @Test
    fun `for existing unstarted offender`() {
      val anOffender = anOffender()
      whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(
        anAssessmentWithEligibilityProgress(),
      )

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
  }

  @Nested
  inner class GetSuitabilityCriterionView {
    @Test
    fun `for a started offender`() {
      val anAssessmentWithEligibilityProgress = anAssessmentWithEligibilityProgress()

      whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(
        anAssessmentWithEligibilityProgress.copy(
          suitabilityProgress = {
            anAssessmentWithEligibilityProgress.suitabilityProgress().mapIndexed { i, it ->
              if (i == 0) {
                it.copy(status = SUITABLE, questions = listOf(it.questions[0].copy(answer = true)))
              } else {
                it
              }
            }
          },
        ),
      )

      val criterion1 = POLICY_1_0.suitabilityCriteria[0]
      val criterion2 = POLICY_1_0.suitabilityCriteria[1]

      val criterionView = service.getSuitabilityCriterionView(PRISON_NUMBER, criterion1.code)

      assertThat(criterionView.assessmentSummary).isEqualTo(
        anAssessmentSummary().copy(
          hdced = anAssessmentWithEligibilityProgress.offender.hdced,
          crd = anAssessmentWithEligibilityProgress.offender.crd,
        ),
      )

      assertThat(criterionView.criterion).isEqualTo(
        SuitabilityCriterionProgress(
          code = criterion1.code,
          taskName = criterion1.name,
          status = SuitabilityStatus.SUITABLE,
          questions = criterion1.questions.map
            {
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
          questions = criterion2.questions.map
            {
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
    fun `for a started offender`() {
      val anAssessmentWithEligibilityProgress = anAssessmentWithEligibilityProgress()

      whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(
        anAssessmentWithEligibilityProgress.copy(
          eligibilityProgress = {
            anAssessmentWithEligibilityProgress.eligibilityProgress().mapIndexed { i, it ->
              if (i == 0) {
                it.copy(status = ELIGIBLE, questions = listOf(it.questions[0].copy(answer = true)))
              } else {
                it
              }
            }
          },
        ),
      )

      val criterion1 = POLICY_1_0.eligibilityCriteria[0]
      val criterion2 = POLICY_1_0.eligibilityCriteria[1]

      val criterionView = service.getEligibilityCriterionView(PRISON_NUMBER, criterion1.code)

      assertThat(criterionView.assessmentSummary).isEqualTo(
        anAssessmentSummary().copy(
          hdced = anAssessmentWithEligibilityProgress.offender.hdced,
          crd = anAssessmentWithEligibilityProgress.offender.crd,
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
      val anAssessmentWithEligibilityProgress =
        anAssessmentWithEligibilityProgress().copy(assessmentEntity = anOffender().currentAssessment())

//      whenever(assessmentLifecycleService.eligibilityAnswerSubmitted(anAssessmentWithEligibilityProgress)).thenReturn(
//        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
//      )

      whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(
        anAssessmentWithEligibilityProgress,
      )

      val criterion = POLICY_1_0.eligibilityCriteria[0]
      val question = criterion.questions.first()

      val criterionChecks = CriterionCheck(
        code = criterion.code,
        type = CriteriaType.ELIGIBILITY,
        answers = mapOf(question.name to true),
      )

      assertThat(anAssessmentWithEligibilityProgress.assessmentEntity.eligibilityCheckResults).isEmpty()

      service.saveAnswer(PRISON_NUMBER, criterionChecks)

      argumentCaptor<Assessment> {
        verify(assessmentRepository).save(capture())

        assertThat(firstValue.status).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
        assertThat(firstValue.eligibilityCheckResults).hasSize(1)
        with(firstValue.eligibilityCheckResults.first()) {
          assertThat(criterionCode).isEqualTo(criterion.code)
          assertThat(criterionType).isEqualTo(ELIGIBILITY)
          assertThat(criterionMet).isEqualTo(false)
          assertThat(criterionVersion).isEqualTo(firstValue.policyVersion)
          assertThat(assessment).isEqualTo(firstValue)
          assertThat(questionAnswers).isEqualTo(mapOf(question.name to true))
        }
      }
    }
  }
}
