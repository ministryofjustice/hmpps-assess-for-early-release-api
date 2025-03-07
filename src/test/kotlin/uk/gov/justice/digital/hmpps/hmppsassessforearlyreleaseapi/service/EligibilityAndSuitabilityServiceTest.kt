package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ResultType.PASSED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithNoProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import java.time.LocalDate

class EligibilityAndSuitabilityServiceTest {
  private val assessmentService = mock<AssessmentService>()
  private val prisonService = Mockito.mock<PrisonService>()
  private val offenderToAssessmentSummaryMapper = OffenderToAssessmentSummaryMapper(prisonService)

  private val service =
    EligibilityAndSuitabilityService(
      PolicyService(),
      assessmentService,
      offenderToAssessmentSummaryMapper,
    )

  @Nested
  inner class GetCaseView {
    @Test
    fun `for existing unstarted offender`() {
      // Given
      val anOffender = anOffender()
      whenever(assessmentService.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)).thenReturn(
        anAssessmentWithNoProgress(),
      )
      whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
      whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

      // When
      val caseView = service.getCaseView(PRISON_NUMBER)

      // Then
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
      // Given
      val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.none(),
        suitabilityProgress = Progress.specifyByIndex(0 to PASSED),
      )

      whenever(assessmentService.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)).thenReturn(
        anAssessmentWithEligibilityProgress,
      )
      whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
      whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

      val criterion1 = POLICY_1_0.suitabilityCriteria[0]
      val criterion2 = POLICY_1_0.suitabilityCriteria[1]

      // When
      val criterionView = service.getSuitabilityCriterionView(PRISON_NUMBER, criterion1.code)

      // then
      assertAssessmentSummary(criterionView.assessmentSummary, anAssessmentWithEligibilityProgress)

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
          agent = PRISON_CA_AGENT,
          lastUpdated = LocalDate.now(),
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
          agent = null,
          lastUpdated = null,
        ),
      )
    }
  }

  @Nested
  inner class GetEligibilityCriterionView {
    @Test
    fun `for a started offender`() {
      // Given

      val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
        ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
        eligibilityProgress = Progress.specifyByIndex(0 to PASSED),
        suitabilityProgress = Progress.none(),
      )

      whenever(assessmentService.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)).thenReturn(
        anAssessmentWithEligibilityProgress,
      )
      whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
      whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

      val criterion1 = POLICY_1_0.eligibilityCriteria[0]
      val criterion2 = POLICY_1_0.eligibilityCriteria[1]

      // When
      val criterionView = service.getEligibilityCriterionView(PRISON_NUMBER, criterion1.code)

      // Then
      assertAssessmentSummary(criterionView.assessmentSummary, anAssessmentWithEligibilityProgress)

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
          agent = PRISON_CA_AGENT,
          lastUpdated = LocalDate.now(),
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
          agent = null,
          lastUpdated = null,
        ),
      )
    }
  }

  @Nested
  inner class SaveAnswer {
    @Test
    fun `for existing unstarted offender`() {
      val assessment = anAssessmentWithNoProgress()

      whenever(assessmentService.getCurrentAssessmentWithEligibilityProgress(PRISON_NUMBER)).thenReturn(assessment)
      doNothing().whenever(assessmentService).transitionAssessment(any<Assessment>(), any(), any())
      val criterion = POLICY_1_0.eligibilityCriteria[0]
      val question = criterion.questions.first()

      val criterionChecks = CriterionCheck(
        code = criterion.code,
        type = CriteriaType.ELIGIBILITY,
        answers = mapOf(question.name to false),
        agent = PRISON_CA_AGENT,
      )

      assertThat(assessment.assessmentEntity.eligibilityCheckResults).isEmpty()

      service.saveAnswer(PRISON_NUMBER, criterionChecks)

      argumentCaptor<Assessment> {
        verify(assessmentService).transitionAssessment(capture(), any(), any())

        assertThat(firstValue.status).isEqualTo(AssessmentStatus.NOT_STARTED)
        assertThat(firstValue.eligibilityCheckResults).hasSize(1)
        with(firstValue.eligibilityCheckResults.first()) {
          assertThat(criterionCode).isEqualTo(criterion.code)
          assertThat(criterionType).isEqualTo(ELIGIBILITY)
          assertThat(criterionMet).isEqualTo(true)
          assertThat(criterionVersion).isEqualTo(firstValue.policyVersion)
          assertThat(assessment.assessmentEntity).isEqualTo(firstValue)
          assertThat(questionAnswers).isEqualTo(mapOf(question.name to false))
        }
      }
    }
  }

  private fun assertAssessmentSummary(
    assessmentSummary: AssessmentSummary,
    assessmentWithEligibilityProgress: AssessmentService.AssessmentWithEligibilityProgress,
  ) {
    val expectedAssessment = assessmentWithEligibilityProgress.assessmentEntity

    assertThat(assessmentSummary).isNotNull
    assertThat(assessmentSummary.crd).isEqualTo(assessmentWithEligibilityProgress.offender.crd)
    assertThat(assessmentSummary.hdced).isEqualTo(assessmentWithEligibilityProgress.offender.hdced)
    assertThat(assessmentSummary.prisonNumber).isEqualTo(assessmentWithEligibilityProgress.offender.prisonNumber)
    assertThat(assessmentSummary.dateOfBirth).isEqualTo(assessmentWithEligibilityProgress.offender.dateOfBirth)
    assertThat(assessmentSummary.forename).isEqualTo(assessmentWithEligibilityProgress.offender.forename)
    assertThat(assessmentSummary.surname).isEqualTo(assessmentWithEligibilityProgress.offender.surname)

    assertThat(assessmentSummary.status).isEqualTo(expectedAssessment.status)
    assertThat(assessmentSummary.optOutReasonOther).isEqualTo(expectedAssessment.optOutReasonOther)
    assertThat(assessmentSummary.optOutReasonType).isEqualTo(expectedAssessment.optOutReasonType)
    assertThat(assessmentSummary.team).isEqualTo(expectedAssessment.team)
    assertThat(assessmentSummary.policyVersion).isEqualTo(expectedAssessment.policyVersion)
    assertThat(assessmentSummary.responsibleCom).isEqualTo(expectedAssessment.responsibleCom?.toSummary())

    assertThat(assessmentSummary.tasks).isEqualTo(
      expectedAssessment.status.tasks().mapValues { (_, tasks) -> tasks.map { TaskProgress(it.task, it.status(expectedAssessment)) } },
    )

    assertThat(assessmentSummary.cellLocation).isEqualTo("A-1-002")
    assertThat(assessmentSummary.location).isEqualTo("Birmingham (HMP)")
    assertThat(assessmentSummary.mainOffense).isEqualTo("Robbery")
  }
}
