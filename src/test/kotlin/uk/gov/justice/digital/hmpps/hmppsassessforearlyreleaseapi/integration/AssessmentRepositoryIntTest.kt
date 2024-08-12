package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository

class AssessmentRepositoryIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var assessmentRepository: AssessmentRepository

  @Test
  fun `should save and get assessments`() {
    val assessment = Assessment(1, 1234L, "G4274GN")
    assessmentRepository.save(assessment)

    val dbAssessment = assessmentRepository.findByIdOrNull(assessment.id) ?: fail("assessment with id ${assessment.id} not found")
    assertThat(dbAssessment.id).isEqualTo(assessment.id)
    assertThat(dbAssessment.prisonerNumber).isEqualTo(assessment.prisonerNumber)
    assertThat(dbAssessment.bookingId).isEqualTo(assessment.bookingId)
    assertThat(dbAssessment.createdTimestamp).isEqualToIgnoringNanos(assessment.createdTimestamp)
    assertThat(dbAssessment.lastUpdatedTimestamp).isEqualToIgnoringNanos(assessment.lastUpdatedTimestamp)
  }
}
