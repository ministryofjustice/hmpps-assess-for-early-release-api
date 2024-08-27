package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import java.time.LocalDate

class OffenderRepositoryIntTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @Test
  fun `should save and get offenders`() {
    val offender = Offender(1, 1234L, "G4274GN", prisonId = "BMI", hdced = LocalDate.now())
    offenderRepository.save(offender)

    val dbOffender = offenderRepository.findByIdOrNull(offender.id) ?: fail("offender with id ${offender.id} not found")
    assertThat(dbOffender).usingRecursiveComparison().ignoringFields("createdTimestamp", "lastUpdatedTimestamp")
  }
}
