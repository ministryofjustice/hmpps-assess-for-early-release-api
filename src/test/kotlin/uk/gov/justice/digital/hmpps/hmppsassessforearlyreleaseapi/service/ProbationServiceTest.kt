package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateCom
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aDeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.DeliusApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.Name
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationSearchApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.User

@ExtendWith(OutputCaptureExtension::class)
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

  @Test
  fun `should log newCom not found if there are no offenders matching crn`(output: CapturedOutput) {
    val crn = "X12345"

    whenever(deliusApiClient.getOffenderManager(crn)).thenReturn(null)
    service.offenderManagerChanged(crn)

    assertThat(output.out).contains("newCom not found for crn: $crn")
  }

  @Test
  fun `should log newCom code if there are offenders matching crn`(output: CapturedOutput) {
    val aDeliusOffenderManager = aDeliusOffenderManager()
    val aCommunityOffenderManager = aCommunityOffenderManager(aDeliusOffenderManager)
    val crn = "X12345"

    whenever(deliusApiClient.getOffenderManager(crn)).thenReturn(aDeliusOffenderManager)
    whenever(staffService.updateComDetails(any())).thenReturn(aCommunityOffenderManager)

    service.offenderManagerChanged(crn)


    assertThat(output.out).contains("responsible officer code for crn $crn is ${aDeliusOffenderManager.code}")
    assertThat(output.out).doesNotContain("newCom not found for crn: $crn")
    assertThat(deliusApiClient.getOffenderManager(crn)).isEqualTo(aDeliusOffenderManager)
    aDeliusOffenderManager.username?.let { verify(deliusApiClient).assignDeliusRole(it) }
    aDeliusOffenderManager.username?.let {
        UpdateCom(
          staffIdentifier = aDeliusOffenderManager.id,
          staffUsername = it,
          staffEmail = aDeliusOffenderManager.email,
          forename = aDeliusOffenderManager.name.forename,
          surname = aDeliusOffenderManager.name.surname,
        )
    }?.let {
      verify(staffService).updateComDetails(
        it
    )
    }
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
