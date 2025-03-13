package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import java.time.LocalDate

class OffenderToAssessmentOverviewSummaryMapperTest {

  private val toTest: OffenderToAssessmentOverviewSummaryMapper = OffenderToAssessmentOverviewSummaryMapper()

  @Test
  fun `maps offender to assessment overview summary correctly with Eligible and Suitable result`() {
    // Given
    val offender = anOffender()
    // When
    val result = toTest.map(offender, PRISON_NAME, aPrisonerSearchPrisoner(), ELIGIBLE, SUITABLE)
    // Assert
    assertAssessmentOverviewSummary(result, offender, "Eligible and Suitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Ineligible result`() {
    // Given
    val offender = anOffender()
    // When
    val result = toTest.map(offender, PRISON_NAME, aPrisonerSearchPrisoner(), INELIGIBLE, SuitabilityStatus.NOT_STARTED)
    // Assert
    assertAssessmentOverviewSummary(result, offender, "Ineligible")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Unsuitable result`() {
    // Given
    val offender = anOffender()
    // When
    val result = toTest.map(offender, PRISON_NAME, aPrisonerSearchPrisoner(), ELIGIBLE, UNSUITABLE)
    // Assert
    assertAssessmentOverviewSummary(result, offender, "Unsuitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Ineligible and Unsuitable result`() {
    // Given
    val offender = anOffender()
    // When
    val result = toTest.map(offender, PRISON_NAME, aPrisonerSearchPrisoner(), INELIGIBLE, UNSUITABLE)
    // Assert
    assertAssessmentOverviewSummary(result, offender, "Ineligible and Unsuitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with null result`() {
    // Given
    val offender = anOffender()
    // When
    val result = toTest.map(offender, PRISON_NAME, aPrisonerSearchPrisoner(), EligibilityStatus.NOT_STARTED, SuitabilityStatus.NOT_STARTED)
    // Assert
    assertAssessmentOverviewSummary(result, offender, null)
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
