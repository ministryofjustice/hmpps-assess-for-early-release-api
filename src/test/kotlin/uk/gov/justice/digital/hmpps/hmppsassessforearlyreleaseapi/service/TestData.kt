package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate

object TestData {

  const val PRISON_NUMBER = "A1234AA"
  const val BOOKING_ID = "123"
  const val FORENAME = "Bob"
  const val SURNAME = "Smith"
  const val PRISON_ID = "AFG"
  const val PRISON_NAME = "Birmingham (HMP)"

  fun anOffender(hdced: LocalDate = LocalDate.now().plusDays(10)): Offender {
    val offender = Offender(
      id = 1,
      bookingId = BOOKING_ID.toLong(),
      prisonNumber = PRISON_NUMBER,
      prisonId = PRISON_ID,
      forename = FORENAME,
      surname = SURNAME,
      dateOfBirth = LocalDate.of(1981, 5, 23),
      hdced = hdced,
    )
    offender.assessments.add(Assessment(offender = offender, policyVersion = PolicyService.CURRENT_POLICY_VERSION.code))
    return offender
  }

  fun anOffenderWithSomeProgress() =
    anOffender().let {
      val currentAssessment = it.currentAssessment()
      val eligibilityCriteria = POLICY_1_0.eligibilityCriteria.first()
      val suitabilityCriteria = POLICY_1_0.suitabilityCriteria.first()
      val assessment = currentAssessment.copy(
        eligibilityCheckResults = mutableSetOf(
          EligibilityCheckResult(
            assessment = currentAssessment,
            criterionCode = eligibilityCriteria.code,
            criterionType = ELIGIBILITY,
            criterionMet = true,
            id = 1,
            criterionVersion = POLICY_1_0.code,
            questionAnswers = mapOf(eligibilityCriteria.questions.first().name to true),
          ),
          EligibilityCheckResult(
            assessment = currentAssessment,
            criterionCode = suitabilityCriteria.code,
            criterionType = SUITABILITY,
            criterionMet = true,
            id = 1,
            criterionVersion = POLICY_1_0.code,
            questionAnswers = mapOf(suitabilityCriteria.questions.first().name to true),
          ),
        ),
      )
      it.copy(assessments = mutableSetOf(assessment))
    }

  fun aPrisonerSearchPrisoner(hdced: LocalDate? = null) = PrisonerSearchPrisoner(
    PRISON_NUMBER,
    bookingId = BOOKING_ID,
    hdced,
    firstName = FORENAME,
    lastName = SURNAME,
    dateOfBirth = LocalDate.of(1981, 5, 23),
    prisonId = PRISON_ID,
  )

  fun anAssessmentSummary() = AssessmentSummary(
    forename = FORENAME,
    surname = SURNAME,
    dateOfBirth = LocalDate.of(1981, 5, 23),
    prisonNumber = PRISON_NUMBER,
    hdced = LocalDate.of(2020, 10, 25),
    crd = LocalDate.of(2022, 3, 21),
    location = PRISON_NAME,
    status = AssessmentStatus.NOT_STARTED,
    policyVersion = "1.0",
  )

  fun anEligibilityCheckDetails() = EligibilityCriterionProgress(
    code = "code-1",
    taskName = "task-1",
    questions = listOf(Question("question-1", answer = true)),
    status = EligibilityStatus.ELIGIBLE,
  )

  fun anSuitabilityCheckDetails() = SuitabilityCriterionProgress(
    code = "code-1",
    taskName = "task-1",
    questions = listOf(Question("question-1", answer = true)),
    status = SuitabilityStatus.SUITABLE,
  )

  fun anAssessmentWithEligibilityProgress() = AssessmentWithEligibilityProgress(
    offender = anOffenderWithSomeProgress(),
    assessmentEntity = anOffenderWithSomeProgress().currentAssessment(),
    prison = "Birmingham (HMP)",
    eligibilityProgress = {
      POLICY_1_0.eligibilityCriteria.map {
        EligibilityCriterionProgress(
          code = it.code,
          taskName = it.name,
          status = NOT_STARTED,
          questions = it.questions.map { question ->
            Question(
              text = question.text,
              hint = question.hint,
              name = question.name,
              answer = null,
            )
          },
        )
      }
    },
    suitabilityProgress = {
      POLICY_1_0.suitabilityCriteria.map {
        SuitabilityCriterionProgress(
          code = it.code,
          taskName = it.name,
          status = SuitabilityStatus.NOT_STARTED,
          questions = it.questions.map { question ->
            Question(
              text = question.text,
              hint = question.hint,
              name = question.name,
              answer = null,
            )
          },
        )
      }
    },
  )
}
