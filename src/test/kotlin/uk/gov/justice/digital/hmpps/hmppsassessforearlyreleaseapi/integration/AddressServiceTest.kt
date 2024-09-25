package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.AddressService
import java.time.LocalDate

const val OS_API_KEY = "os-places-api-key"

class AddressServiceTest : SqsIntegrationTestBase() {
  @Autowired
  private lateinit var addressService: AddressService

  @Test
  fun `should get addresses by post code`() {
    val postcode = "AG121RW"
    osPlacesMockServer.stubGetAddressesForPostcode(postcode)

    val addresses = addressService.getAddressesForPostcode(postcode)
    assertThat(addresses).size().isEqualTo(3)
    assertThat(addresses[0].uprn).isEqualTo("100120991537")
    assertThat(addresses[1].postCode).isEqualTo("AG12 1RW")
    assertThat(addresses[2].xCoordinate).isEqualTo(401003.0)
    assertThat(addresses[2].lastUpdateDate).isEqualTo(LocalDate.of(2021, 5, 1))
  }

  @Test
  fun `should get address by post uprn`() {
    val uprn = "200010019924"
    osPlacesMockServer.stubGetAddressByUprn(uprn)

    val address = addressService.getAddressForUprn(uprn)
    assertThat(address.postCode).isEqualTo("SO16 0AS")
    assertThat(address.uprn).isEqualTo(uprn)
    assertThat(address.yCoordinate).isEqualTo(115541.95)
  }

  private companion object {
    val osPlacesMockServer = OsPlacesMockServer(OS_API_KEY)

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      osPlacesMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      osPlacesMockServer.stop()
    }
  }
}
