package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
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

  @Autowired
  private lateinit var residentRepository: ResidentRepository

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
    assertThat(addresses[2].xcoordinate).isEqualTo(401003.0)
    assertThat(addresses[2].addressLastUpdated).isEqualTo(LocalDate.of(2021, 5, 1))
  }

  @Test
  fun `should return empty list of places for invalid post code`() {
    val postcode = "INVALID"
    osPlacesMockServer.stubGetAddressesForPostcodeBadRequest(postcode)

    val addresses = addressService.getAddressesForPostcode(postcode)
    assertThat(addresses).isEmpty()
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
    assertThat(address.firstLine).isEqualTo("ORDNANCE SURVEY, 4 ADANAC DRIVE")

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
    val preferencePriority = AddressPreferencePriority.FIRST
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
    val preferencePriority = AddressPreferencePriority.SECOND
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

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/a-standard-address-check-request.sql",
  )
  @Test
  fun `should add, edit and remove a resident to a standard address check request`() {
    val standardAddressCheckRequest = standardAddressCheckRequestRepository.findAll().first()

    val addMainResident = AddResidentRequest(
      residentId = 1,
      forename = "Joshua",
      surname = "Cook",
      phoneNumber = "07739754284",
      relation = "Father",
      dateOfBirth = LocalDate.now().minusYears(24),
      age = 24,
      isMainResident = true,
      isOffender = false,
    )

    val addOtherResident1 = AddResidentRequest(
      residentId = 2,
      forename = "Tom",
      surname = "Cook",
      phoneNumber = "07739759898",
      relation = "Brother",
      dateOfBirth = LocalDate.now().minusYears(24),
      age = 24,
      isMainResident = false,
      isOffender = false,
    )

    val addOtherResident2 = AddResidentRequest(
      residentId = null,
      forename = "John",
      surname = "Cesena",
      phoneNumber = "07739759898",
      relation = "Son",
      dateOfBirth = LocalDate.now().minusYears(24),
      age = 22,
      isMainResident = false,
      isOffender = false,
    )

    val residentSummary = addressService.addResidents(TestData.PRISON_NUMBER, standardAddressCheckRequest.id, listOf(addMainResident, addOtherResident1, addOtherResident2))
    assertThat(residentSummary).isNotNull

    val dbResidentAfterUpdate = residentRepository.findAll().sortedBy { it.id }
    assertThat(dbResidentAfterUpdate).isNotNull

    with(dbResidentAfterUpdate.last()) {
      assertThat(id).isEqualTo(3)
      assertThat(forename).isEqualTo(addOtherResident2.forename)
      assertThat(surname).isEqualTo(addOtherResident2.surname)
    }

    with(dbResidentAfterUpdate[1]) {
      assertThat(id).isEqualTo(2)
      assertThat(forename).isEqualTo(addOtherResident1.forename)
      assertThat(surname).isEqualTo(addOtherResident1.surname)
      assertThat(isMainResident).isEqualTo(addOtherResident1.isMainResident)
      assertThat(relation).isEqualTo(addOtherResident1.relation)
    }

    with(dbResidentAfterUpdate.first()) {
      assertThat(id).isEqualTo(1)
      assertThat(forename).isEqualTo(addMainResident.forename)
      assertThat(surname).isEqualTo(addMainResident.surname)
      assertThat(isMainResident).isEqualTo(addMainResident.isMainResident)
      assertThat(relation).isEqualTo(addMainResident.relation)
    }
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/a-standard-address-check-request.sql",
  )
  @Test
  fun `should add resident without relation if an offender`() {
    val standardAddressCheckRequest = standardAddressCheckRequestRepository.findAll().first()

    val addResidentRequest = AddResidentRequest(
      residentId = null,
      forename = "Jane",
      surname = "Doe",
      phoneNumber = "07739754284",
      relation = null,
      dateOfBirth = LocalDate.now().minusYears(30),
      age = 30,
      isMainResident = false,
      isOffender = true,
    )

    val residentSummary = addressService.addResidents(TestData.PRISON_NUMBER, standardAddressCheckRequest.id, listOf(addResidentRequest))
    assertThat(residentSummary).isNotNull
    assertThat(residentSummary).hasSize(1)
    assertThat(residentSummary.first().relation).isNull()
    assertThat(residentSummary.first().isOffender).isTrue()
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/a-standard-address-check-request.sql",
  )
  @Test
  fun `should throw a not found exception if standard address check request is not linked to prisoner`() {
    val standardAddressCheckRequest = standardAddressCheckRequestRepository.findAll().first()
    val prisonNumber = "G9374FU"

    val addResidentRequest = AddResidentRequest(
      residentId = 1,
      forename = "Joshua",
      surname = "Cook",
      phoneNumber = "07739754284",
      relation = "Father",
      dateOfBirth = LocalDate.now().minusYears(24),
      age = 24,
      isMainResident = true,
      isOffender = false,
    )

    assertThrows<ItemNotFoundException> { addressService.addResidents(prisonNumber, standardAddressCheckRequest.id, listOf(addResidentRequest)) }
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
