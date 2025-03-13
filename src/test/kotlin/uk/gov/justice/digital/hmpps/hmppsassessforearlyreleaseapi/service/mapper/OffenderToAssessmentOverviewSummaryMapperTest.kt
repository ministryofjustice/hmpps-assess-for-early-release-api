import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PolicyService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.DAYS_TO_ADD
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentOverviewSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import java.time.LocalDate

class OffenderToAssessmentOverviewSummaryMapperTest {

  private var prisonService = mock<PrisonService>()
  private var policyService = PolicyService()

  private val toTest: OffenderToAssessmentOverviewSummaryMapper = OffenderToAssessmentOverviewSummaryMapper(prisonService, policyService)

  @BeforeEach
  fun setup() {
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
    whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Eligible and Suitable result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      ELIGIBLE_AND_SUITABLE,
      eligibilityProgress = Progress.allSuccessful(),
      suitabilityProgress = Progress.allSuccessful(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender

    // When
    val result = toTest.map(offender)

    // Assert
    assertAssessmentOverviewSummary(result, offender, "Eligible and Suitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Ineligible result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      INELIGIBLE_OR_UNSUITABLE,
      eligibilityProgress = Progress.allFailed(),
      suitabilityProgress = Progress.none(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender

    // When
    val result = toTest.map(offender)

    // Assert
    assertAssessmentOverviewSummary(result, offender, "Ineligible")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Unsuitable result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      INELIGIBLE_OR_UNSUITABLE,
      eligibilityProgress = Progress.allSuccessful(),
      suitabilityProgress = Progress.allFailed(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender

    // When
    val result = toTest.map(offender)

    // Assert
    assertAssessmentOverviewSummary(result, offender, "Unsuitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with Ineligible and Unsuitable result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      INELIGIBLE_OR_UNSUITABLE,
      eligibilityProgress = Progress.allFailed(),
      suitabilityProgress = Progress.allFailed(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender

    // When
    val result = toTest.map(offender)

    // Assert
    assertAssessmentOverviewSummary(result, offender, "Ineligible and Unsuitable")
  }

  @Test
  fun `maps offender to assessment overview summary correctly with null result`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      NOT_STARTED,
      eligibilityProgress = Progress.none(),
      suitabilityProgress = Progress.none(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender

    // When
    val result = toTest.map(offender)

    // Assert
    assertAssessmentOverviewSummary(result, offender, null)
  }

  @Test
  fun `when prisoner not found exception is thrown`() {
    // Given
    val offender = anOffender()
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf())

    // When
    val result = assertThrows<ItemNotFoundException> {
      toTest.map(offender)
    }

    // Assert
    assertThat(result).isExactlyInstanceOf(ItemNotFoundException::class.java)
    assertThat(result.message).isEqualTo("Could not find prisoner details for A1234AA")
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
