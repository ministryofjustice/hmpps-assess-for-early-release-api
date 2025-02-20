package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.PrisonerNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService

class OffenderToAssessmentSummaryMapperTest {

  private var prisonService = mock<PrisonService>()

  private val toTest: OffenderToAssessmentSummaryMapper = OffenderToAssessmentSummaryMapper(prisonService)

  @Test
  fun `maps offender to assessment summary correctly`() {
    // Given
    val offender = anOffender()
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))

    // When
    val result = toTest.map(offender)

    // Assert
    assertAssessmentSummary(result, offender)
  }

  @Test
  fun `when prisoner not found exception is thrown`() {
    // Given
    val offender = anOffender()
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf())

    // When
    val result = assertThrows<Throwable> {
      toTest.map(offender)
    }

    // Assert
    assertThat(result).isExactlyInstanceOf(PrisonerNotFoundException::class.java)
    assertThat((result as PrisonerNotFoundException).prisonCode).isEqualTo(PRISON_NUMBER)
  }

  private fun assertAssessmentSummary(
    assessmentSummary: AssessmentSummary,
    offender: Offender,
  ) {
    val expectedAssessment = offender.currentAssessment()

    assertThat(assessmentSummary).isNotNull
    assertThat(assessmentSummary.crd).isEqualTo(offender.crd)
    assertThat(assessmentSummary.hdced).isEqualTo(offender.hdced)
    assertThat(assessmentSummary.prisonNumber).isEqualTo(offender.prisonNumber)
    assertThat(assessmentSummary.dateOfBirth).isEqualTo(offender.dateOfBirth)
    assertThat(assessmentSummary.forename).isEqualTo(offender.forename)
    assertThat(assessmentSummary.surname).isEqualTo(offender.surname)

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
