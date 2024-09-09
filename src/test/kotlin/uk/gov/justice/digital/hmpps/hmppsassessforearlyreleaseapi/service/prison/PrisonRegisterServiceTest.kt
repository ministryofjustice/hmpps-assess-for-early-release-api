package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PrisonRegisterServiceTest {
  private val prisonRegisterApiClient: PrisonRegisterApiClient = mock<PrisonRegisterApiClient>()

  private val prisonRegisterService = PrisonRegisterService(prisonRegisterApiClient)

  @Test
  fun `should get a map of prison ids to names`() {
    whenever(prisonRegisterApiClient.getPrisons()).thenReturn(
      listOf(
        Prison("BMI", "Birmingham (HMP)"),
        Prison("LCI", "Leicester (HMP)"),
        Prison("WEI", "Wealstun (HMP)"),
      ),
    )

    val prisonIdNameMap = prisonRegisterService.getPrisonIdsAndNames()
    assertThat(prisonIdNameMap).isEqualTo(
      mapOf(
        "BMI" to "Birmingham (HMP)",
        "LCI" to "Leicester (HMP)",
        "WEI" to "Wealstun (HMP)",
      ),
    )
  }
}
