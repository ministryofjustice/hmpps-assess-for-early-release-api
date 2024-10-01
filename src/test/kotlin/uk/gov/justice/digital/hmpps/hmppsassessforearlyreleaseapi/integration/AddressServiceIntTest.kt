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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.AddressService
import java.time.LocalDate

const val OS_API_KEY = "os-places-api-key"

class AddressServiceTest : SqsIntegrationTestBase() {
  @Autowired
  private lateinit var addressRepository: AddressRepository

  @Autowired
  private lateinit var casCheckRequestRepository: CasCheckRequestRepository

  @Autowired
  private lateinit var standardAddressCheckRequestRepository: StandardAddressCheckRequestRepository

  @Autowired
  private lateinit var addressService: AddressService

  @BeforeEach
  fun resetMocks() {
    osPlacesMockServer.resetAll()
  }

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

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
    "classpath:test_data/an-address.sql",
  )
  @Test
  fun `should add a standard address check request`() {
    val prisonNumber = "C1234CC"
    val caAdditionalInfo = "ca info"
    val ppAdditionalInfo = "pp info"
    val preferencePriority = AddressPreferencePriority.THIRD
    val uprn = "200010019924"
    val addStandardAddressCheckRequest = AddStandardAddressCheckRequest(
      caAdditionalInfo = caAdditionalInfo,
      ppAdditionalInfo = ppAdditionalInfo,
      preferencePriority = preferencePriority,
      addressUprn = uprn,
    )

    addressService.addStandardAddressCheckRequest(prisonNumber, addStandardAddressCheckRequest)

    val dbStandardAddressCheckRequests = standardAddressCheckRequestRepository.findAll()
    assertThat(dbStandardAddressCheckRequests).hasSize(1)
    val dbAddressCheckRequest = dbStandardAddressCheckRequests.first()
    assertThat(dbAddressCheckRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
    assertThat(dbAddressCheckRequest.address.uprn).isEqualTo(uprn)
    assertThat(dbAddressCheckRequest.caAdditionalInfo).isEqualTo(caAdditionalInfo)
    assertThat(dbAddressCheckRequest.ppAdditionalInfo).isEqualTo(ppAdditionalInfo)
    assertThat(dbAddressCheckRequest.preferencePriority).isEqualTo(preferencePriority)
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
    "classpath:test_data/an-address.sql",
  )
  @Test
  fun `should add a cas check request`() {
    val prisonNumber = "C1234CC"
    val caAdditionalInfo = "ca info"
    val ppAdditionalInfo = "pp info"
    val preferencePriority = AddressPreferencePriority.FOURTH
    val addCasCheckRequest = AddCasCheckRequest(
      caAdditionalInfo = caAdditionalInfo,
      ppAdditionalInfo = ppAdditionalInfo,
      preferencePriority = preferencePriority,
    )

    addressService.addCasCheckRequest(prisonNumber, addCasCheckRequest)

    val dbCasCheckRequests = casCheckRequestRepository.findAll()
    assertThat(dbCasCheckRequests).hasSize(1)
    val dbCasCheckRequest = dbCasCheckRequests.first()
    assertThat(dbCasCheckRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
    assertThat(dbCasCheckRequest.caAdditionalInfo).isEqualTo(caAdditionalInfo)
    assertThat(dbCasCheckRequest.ppAdditionalInfo).isEqualTo(ppAdditionalInfo)
    assertThat(dbCasCheckRequest.preferencePriority).isEqualTo(preferencePriority)
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
