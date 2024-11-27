package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aDeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aUpdateCom

class StaffServiceTest {
  private val staffRepository = mock<StaffRepository>()

  private val service =
    StaffService(staffRepository)

  @BeforeEach
  fun reset() {
    reset(staffRepository)
    whenever(staffRepository.saveAndFlush(any())).thenAnswer { it.arguments[0] }
  }

  @Nested
  inner class `Update COM tests` {
    @Test
    fun `updates existing COM with new details`() {
      val aDeliusOffenderManager = aDeliusOffenderManager()
      val aCommunityOffenderManager = aCommunityOffenderManager(aDeliusOffenderManager)
      val aUpdateCom = aUpdateCom(aDeliusOffenderManager)!!
      val expectedCom = aCommunityOffenderManager.copy(
        staffIdentifier = 3000,
        username = "JBLOGGS",
        email = "jbloggs123@probation.gov.uk",
      )

      whenever(staffRepository.findByStaffIdentifierOrUsernameIgnoreCase(any(), any()))
        .thenReturn(
          listOf(
            expectedCom.copy(
              staffIdentifier = 2000,
              username = "joebloggs",
              email = "jbloggs@probation.gov.uk",
            ),
          ),
        )

      val comDetails = aUpdateCom.copy(
          staffIdentifier = 3000,
          staffUsername = "jbloggs",
          staffEmail = "jbloggs123@probation.gov.uk",
      )

      service.updateComDetails(comDetails)

      argumentCaptor<CommunityOffenderManager>().apply {
        verify(staffRepository, times(1)).findByStaffIdentifierOrUsernameIgnoreCase(3000, "jbloggs")
        verify(staffRepository, times(1)).saveAndFlush(capture())

        assertThat(firstValue).usingRecursiveComparison().ignoringFields("lastUpdatedTimestamp")
          .isEqualTo(expectedCom)
      }
    }

    @Test
    fun `does not update COM with same details`() {
      val aDeliusOffenderManager = aDeliusOffenderManager()
      val aCommunityOffenderManager = aCommunityOffenderManager(aDeliusOffenderManager)
      val aUpdateCom = aUpdateCom(aDeliusOffenderManager)!!

      whenever(staffRepository.findByStaffIdentifierOrUsernameIgnoreCase(any(), any()))
        .thenReturn(
          listOf(
            aCommunityOffenderManager.copy(
              staffIdentifier = 2000,
              username = "joebloggs",
            ),
          ),
        )

      val comDetails = aUpdateCom.copy(
        staffIdentifier = 2000,
        staffUsername = "JOEBLOGGS",
      )

      service.updateComDetails(comDetails)

      verify(staffRepository, times(1)).findByStaffIdentifierOrUsernameIgnoreCase(2000, "JOEBLOGGS")
      verify(staffRepository, times(0)).saveAndFlush(any())
    }

    @Test
    fun `adds a new existing COM if it doesnt exist`() {
      val aDeliusOffenderManager = aDeliusOffenderManager()
      val aCommunityOffenderManager = aCommunityOffenderManager(aDeliusOffenderManager)
      val aUpdateCom = aUpdateCom(aDeliusOffenderManager)!!

      val expectedCom = aCommunityOffenderManager.copy(
        staffIdentifier = 3000,
        username = "JBLOGGS",
        email = "jbloggs123@probation.gov.uk",
      )

      whenever(
        staffRepository.findByStaffIdentifierOrUsernameIgnoreCase(
          any(),
          any(),
        ),
      ).thenReturn(null)

      val comDetails = aUpdateCom.copy(
        staffIdentifier = 3000,
        staffUsername = "jbloggs",
        staffEmail = "jbloggs123@probation.gov.uk",
      )

      service.updateComDetails(comDetails)

      argumentCaptor<CommunityOffenderManager>().apply {
        verify(staffRepository, times(1)).findByStaffIdentifierOrUsernameIgnoreCase(3000, "jbloggs")
        verify(staffRepository, times(1)).saveAndFlush(capture())

        assertThat(firstValue).usingRecursiveComparison().ignoringFields("id", "lastUpdatedTimestamp")
          .isEqualTo(expectedCom)
      }
    }

    @Test
    fun `updates existing COM with new staffIdentifier`() {
      val aDeliusOffenderManager = aDeliusOffenderManager()
      val aCommunityOffenderManager = aCommunityOffenderManager(aDeliusOffenderManager)
      val aUpdateCom = aUpdateCom(aDeliusOffenderManager)!!

      val expectedCom = aCommunityOffenderManager.copy(
        staffIdentifier = 2001,
        username = "JOEBLOGGS",
        email = "jbloggs123@probation.gov.uk",
      )

      whenever(staffRepository.findByStaffIdentifierOrUsernameIgnoreCase(any(), any()))
        .thenReturn(
          listOf(
            aCommunityOffenderManager.copy(
              staffIdentifier = 2000,
              username = "JOEBLOGGS",
              email = "jbloggs@probation.gov.uk",
            ),
          ),
        )

      val comDetails = aUpdateCom.copy(
        staffIdentifier = 2001,
        staffUsername = "JOEBLOGGS",
        staffEmail = "jbloggs123@probation.gov.uk",
      )

      service.updateComDetails(comDetails)

      argumentCaptor<CommunityOffenderManager>().apply {
        verify(staffRepository, times(1)).findByStaffIdentifierOrUsernameIgnoreCase(2001, "JOEBLOGGS")
        verify(staffRepository, times(1)).saveAndFlush(capture())

        assertThat(firstValue).usingRecursiveComparison().ignoringFields("lastUpdatedTimestamp")
          .isEqualTo(expectedCom)
      }
    }
  }
}
