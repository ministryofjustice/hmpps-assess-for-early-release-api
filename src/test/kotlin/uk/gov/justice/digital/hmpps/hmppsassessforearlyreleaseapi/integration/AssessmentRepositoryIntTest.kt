package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
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

    val dbAssessment = assessmentRepository.findByIdOrNull(assessment.id)
    assertThat(dbAssessment).isEqualTo(assessment)
  }
}
