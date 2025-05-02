package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anStatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService

class OffenderToAssessmentSummaryMapperTest {

  private var prisonService = mock<PrisonService>()

  private val toTest: OffenderToAssessmentSummaryMapper = OffenderToAssessmentSummaryMapper(prisonService)

  @Test
  fun `maps offender to assessment summary correctly`() {
    // Given
    val offender = anOffender()
    val assessment = offender.assessments.first()
    assessment.lastUpdateByUserEvent = anStatusChangedEvent(assessment)

    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
    whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)
    // When
    val result = toTest.map(assessment)

    // Assert
    assertAssessmentSummary(result, assessment)
  }

  @Test
  fun `when prisoner not found exception is thrown`() {
    // Given
    val offender = anOffender()
    val assessment = offender.assessments.first()
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf())

    // When
    val result = assertThrows<Throwable> {
      toTest.map(assessment)
    }

    // Assert
    assertThat(result).isExactlyInstanceOf(ItemNotFoundException::class.java)
    assertThat((result as ItemNotFoundException).message).isEqualTo("Could not find prisoner details for A1234AA")
  }

  private fun assertAssessmentSummary(
    assessmentSummary: AssessmentSummary,
    expectedAssessment: Assessment,
  ) {
    val offender = expectedAssessment.offender

    assertThat(assessmentSummary).isNotNull
    assertThat(assessmentSummary.prisonNumber).isEqualTo(offender.prisonNumber)
    assertThat(assessmentSummary.dateOfBirth).isEqualTo(offender.dateOfBirth)
    assertThat(assessmentSummary.forename).isEqualTo(offender.forename)
    assertThat(assessmentSummary.surname).isEqualTo(offender.surname)

    assertThat(assessmentSummary.status).isEqualTo(expectedAssessment.status)
    assertThat(assessmentSummary.optOutReasonOther).isEqualTo(expectedAssessment.optOutReasonOther)
    assertThat(assessmentSummary.optOutReasonType).isEqualTo(expectedAssessment.optOutReasonType)
    assertThat(assessmentSummary.postponementReasons).isEqualTo(expectedAssessment.postponementReasons)
    assertThat(assessmentSummary.teamCode).isEqualTo(expectedAssessment.teamCode)
    assertThat(assessmentSummary.policyVersion).isEqualTo(expectedAssessment.policyVersion)
    assertThat(assessmentSummary.responsibleCom).isEqualTo(expectedAssessment.responsibleCom?.toSummary())
    assertThat(assessmentSummary.hdced).isEqualTo(expectedAssessment.hdced)
    assertThat(assessmentSummary.crd).isEqualTo(expectedAssessment.crd)

    assertThat(assessmentSummary.tasks).isEqualTo(expectedAssessment.tasks())

    assertThat(assessmentSummary.cellLocation).isEqualTo("A-1-002")
    assertThat(assessmentSummary.location).isEqualTo("Birmingham (HMP)")
    assertThat(assessmentSummary.mainOffense).isEqualTo("Robbery")
    assertThat(assessmentSummary.lastUpdateBy).isEqualTo("prison user")
  }
}
