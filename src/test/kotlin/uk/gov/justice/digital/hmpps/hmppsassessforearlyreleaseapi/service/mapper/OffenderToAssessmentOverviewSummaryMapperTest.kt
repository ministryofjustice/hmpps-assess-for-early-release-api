package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithSomeProgress
import java.time.LocalDate

class OffenderToAssessmentOverviewSummaryMapperTest {

  private val toTest: OffenderToAssessmentOverviewSummaryMapper = OffenderToAssessmentOverviewSummaryMapper()

  @Test
  fun `maps offender to assessment overview summary correctly with Eligible and Suitable result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      ELIGIBLE_AND_SUITABLE,
      eligibilityProgress = Progress.allSuccessful(),
      suitabilityProgress = Progress.allSuccessful(),
    )
    // When
    val result = toTest.map(anAssessmentWithEligibilityProgress, PRISON_NAME, aPrisonerSearchPrisoner())
    // Assert
    assertAssessmentOverviewSummary(result, anAssessmentWithEligibilityProgress.offender, "Eligible and Suitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Ineligible result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      INELIGIBLE_OR_UNSUITABLE,
      eligibilityProgress = Progress.allFailed(),
      suitabilityProgress = Progress.allSuccessful(),
    )
    // When
    val result = toTest.map(anAssessmentWithEligibilityProgress, PRISON_NAME, aPrisonerSearchPrisoner())
    // Assert
    assertAssessmentOverviewSummary(result, anAssessmentWithEligibilityProgress.offender, "Ineligible")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Unsuitable result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      INELIGIBLE_OR_UNSUITABLE,
      eligibilityProgress = Progress.allSuccessful(),
      suitabilityProgress = Progress.allFailed(),
    )
    // When
    val result = toTest.map(anAssessmentWithEligibilityProgress, PRISON_NAME, aPrisonerSearchPrisoner())
    // Assert
    assertAssessmentOverviewSummary(result, anAssessmentWithEligibilityProgress.offender, "Unsuitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Ineligible and Unsuitable result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      INELIGIBLE_OR_UNSUITABLE,
      eligibilityProgress = Progress.allFailed(),
      suitabilityProgress = Progress.allFailed(),
    )
    // When
    val result = toTest.map(anAssessmentWithEligibilityProgress, PRISON_NAME, aPrisonerSearchPrisoner())
    // Assert
    assertAssessmentOverviewSummary(result, anAssessmentWithEligibilityProgress.offender, "Ineligible and Unsuitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with null result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS,
      eligibilityProgress = Progress.none(),
      suitabilityProgress = Progress.none(),
    )
    // When
    val result = toTest.map(anAssessmentWithEligibilityProgress, PRISON_NAME, aPrisonerSearchPrisoner())
    // Assert
    assertAssessmentOverviewSummary(result, anAssessmentWithEligibilityProgress.offender, null)
  }

  private fun assertAssessmentOverviewSummary(
    assessmentOverviewSummary: AssessmentOverviewSummary,
    offender: Offender,
    result: String?,
  ) {
    val expectedAssessment = offender.currentAssessment()

    assertThat(assessmentOverviewSummary).isNotNull
    assertThat(assessmentOverviewSummary.crd).isEqualTo(offender.crd)
    assertThat(assessmentOverviewSummary.hdced).isEqualTo(offender.hdced)
    assertThat(assessmentOverviewSummary.prisonNumber).isEqualTo(offender.prisonNumber)
    assertThat(assessmentOverviewSummary.dateOfBirth).isEqualTo(offender.dateOfBirth)
    assertThat(assessmentOverviewSummary.forename).isEqualTo(offender.forename)
    assertThat(assessmentOverviewSummary.surname).isEqualTo(offender.surname)

    assertThat(assessmentOverviewSummary.status).isEqualTo(expectedAssessment.status)
    assertThat(assessmentOverviewSummary.optOutReasonOther).isEqualTo(expectedAssessment.optOutReasonOther)
    assertThat(assessmentOverviewSummary.optOutReasonType).isEqualTo(expectedAssessment.optOutReasonType)
    assertThat(assessmentOverviewSummary.team).isEqualTo(expectedAssessment.team)
    assertThat(assessmentOverviewSummary.policyVersion).isEqualTo(expectedAssessment.policyVersion)
    assertThat(assessmentOverviewSummary.responsibleCom).isEqualTo(expectedAssessment.responsibleCom?.toSummary())

    assertThat(assessmentOverviewSummary.tasks).isEqualTo(
      expectedAssessment.status.tasks().mapValues { (_, tasks) -> tasks.map { TaskProgress(it.task, it.status(expectedAssessment)) } },
    )

    assertThat(assessmentOverviewSummary.cellLocation).isEqualTo("A-1-002")
    assertThat(assessmentOverviewSummary.location).isEqualTo("Birmingham (HMP)")
    assertThat(assessmentOverviewSummary.mainOffense).isEqualTo("Robbery")
    assertThat(assessmentOverviewSummary.toDoEligibilityAndSuitabilityBy).isEqualTo(LocalDate.now().plusDays(DAYS_TO_ADD))
    assertThat(assessmentOverviewSummary.result).isEqualTo(result)
  }
}
