package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.DeliusApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.Name
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationSearchApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.User

class ProbationServiceTest {
  private val deliusApiClient = mock<DeliusApiClient>()
  private val probationSearchApiClient = mock<ProbationSearchApiClient>()
  private val staffService = mock<StaffService>()

  private val service: ProbationService =
    ProbationService(
      deliusApiClient,
      probationSearchApiClient,
      staffService,
    )

  @Test
  fun `should get staff details by username`() {
    val comUsername = "com-user"

    whenever(deliusApiClient.getStaffDetailsByUsername(comUsername)).thenReturn(comUser)

    val com = service.getStaffDetailsByUsername(comUsername)

    assertThat(com).isEqualTo(comUser)
  }

  private companion object {
    val comUser = User(
      id = 2000,
      username = "com-user",
      email = "comuser@probation.gov.uk",
      name = Name(
        forename = "com",
        surname = "user",
      ),
      teams = emptyList(),
      code = "AB00001",
    )
  }
}
