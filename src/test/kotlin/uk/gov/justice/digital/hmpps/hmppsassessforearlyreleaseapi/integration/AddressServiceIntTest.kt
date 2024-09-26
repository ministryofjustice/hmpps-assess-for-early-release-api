package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.AddressService
import java.time.LocalDate

const val OS_API_KEY = "os-places-api-key"

class AddressServiceTest : SqsIntegrationTestBase() {
  @Autowired
  private lateinit var addressRepository: AddressRepository

  @Autowired
  private lateinit var addressService: AddressService

  @Test
  fun `should get addresses by post code`() {
    val postcode = "AG121RW"
    osPlacesMockServer.stubGetAddressesForPostcode(postcode)

    val addresses = addressService.getAddressesForPostcode(postcode)
    assertThat(addresses).size().isEqualTo(3)
    assertThat(addresses[0].uprn).isEqualTo("100120991537")
    assertThat(addresses[1].postcode).isEqualTo(postcode)
    assertThat(addresses[2].xCoordinate).isEqualTo(401003.0)
    assertThat(addresses[2].addressLastUpdated).isEqualTo(LocalDate.of(2021, 5, 1))
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/an-address.sql",
  )
  @Test
  fun `should get address from OS places API when address doesn't exist in database`() {
    val uprn = "200010019924"
    osPlacesMockServer.stubGetAddressByUprn(uprn)

    val address = addressService.getAddressForUprn(uprn)
    assertThat(address.postcode).isEqualTo("SO16 0AS")
    assertThat(address.uprn).isEqualTo(uprn)
    assertThat(address.firstLine).isEqualTo("4 ADANAC DRIVE")

    osPlacesMockServer.verify(1, getRequestedFor(urlEqualTo("/uprn?uprn=$uprn&key=$OS_API_KEY")))
    val savedAddress = addressRepository.findByUprn(uprn)
    assertThat(savedAddress).isNotNull()
    assertThat(savedAddress?.uprn).isEqualTo(uprn)
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/an-address.sql",
  )
  @Test
  fun `should get address from database when it already exists`() {
    val uprn = "200010019924"

    val address = addressService.getAddressForUprn(uprn)

    osPlacesMockServer.verify(0, getRequestedFor(urlEqualTo("/uprn?uprn=$uprn&key=$OS_API_KEY")))

    assertThat(address.postcode).isEqualTo("SO16 0AS")
    assertThat(address.uprn).isEqualTo(uprn)
    assertThat(address.firstLine).isEqualTo("4 ADANAC DRIVE")

    val savedAddress = addressRepository.findByUprn(uprn)
    assertThat(savedAddress).isNotNull()
    assertThat(savedAddress?.uprn).isEqualTo(uprn)
    assertThat(savedAddress?.country).isEqualTo("England")
  }

  private companion object {
    val osPlacesMockServer = OsPlacesMockServer(OS_API_KEY)

    @BeforeEach
    fun resetMocks() {
      osPlacesMockServer.resetAll()
    }

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
