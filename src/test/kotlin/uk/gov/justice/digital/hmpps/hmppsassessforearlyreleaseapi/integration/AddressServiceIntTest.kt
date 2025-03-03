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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentEventRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
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

  @Autowired
  private lateinit var assessmentEventRepository: AssessmentEventRepository

  @Autowired
  private lateinit var curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository

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

    addressService.addStandardAddressCheckRequest(prisonNumber, addStandardAddressCheckRequest, PRISON_CA_AGENT.toEntity())

    val dbStandardAddressCheckRequests = standardAddressCheckRequestRepository.findAll()
    assertThat(dbStandardAddressCheckRequests).hasSize(1)
    val dbAddressCheckRequest = dbStandardAddressCheckRequests.first()
    assertThat(dbAddressCheckRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
    assertThat(dbAddressCheckRequest.address.uprn).isEqualTo(uprn)
    assertThat(dbAddressCheckRequest.caAdditionalInfo).isEqualTo(caAdditionalInfo)
    assertThat(dbAddressCheckRequest.ppAdditionalInfo).isEqualTo(ppAdditionalInfo)
    assertThat(dbAddressCheckRequest.preferencePriority).isEqualTo(preferencePriority)

    val assessmentEvents = assessmentEventRepository.findByAssessmentId(dbAddressCheckRequest.assessment.id)
    assertThat(assessmentEvents).isNotEmpty
    assertThat(assessmentEvents).hasSize(1)

    val firstEvent = assessmentEvents.first() as GenericChangedEvent
    assertThat(firstEvent.eventType).isEqualTo(AssessmentEventType.ADDRESS_UPDATED)
    assertThat(firstEvent.changes["standardAddressCheckRequest"]).isEqualTo(
      mapOf(
        "caAdditionalInfo" to caAdditionalInfo,
        "ppAdditionalInfo" to ppAdditionalInfo,
        "preferencePriority" to preferencePriority.toString(),
        "addressUprn" to uprn,
      ),
    )
    assertThat(firstEvent.agent.role).isEqualTo(PRISON_CA_AGENT.role)
    assertThat(firstEvent.agent.fullName).isEqualTo(PRISON_CA_AGENT.fullName)
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

    addressService.addCasCheckRequest(prisonNumber, addCasCheckRequest, PRISON_CA_AGENT.toEntity())

    val dbCasCheckRequests = casCheckRequestRepository.findAll()
    assertThat(dbCasCheckRequests).hasSize(1)
    val dbCasCheckRequest = dbCasCheckRequests.first()
    assertThat(dbCasCheckRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
    assertThat(dbCasCheckRequest.caAdditionalInfo).isEqualTo(caAdditionalInfo)
    assertThat(dbCasCheckRequest.ppAdditionalInfo).isEqualTo(ppAdditionalInfo)
    assertThat(dbCasCheckRequest.preferencePriority).isEqualTo(preferencePriority)

    val assessmentEvents = assessmentEventRepository.findByAssessmentId(dbCasCheckRequest.assessment.id)
    assertThat(assessmentEvents).isNotEmpty
    assertThat(assessmentEvents).hasSize(1)

    val firstEvent = assessmentEvents.first() as GenericChangedEvent
    assertThat(firstEvent.eventType).isEqualTo(AssessmentEventType.ADDRESS_UPDATED)
    assertThat(firstEvent.changes["casCheckRequest"]).isEqualTo(
      mapOf(
        "caAdditionalInfo" to caAdditionalInfo,
        "ppAdditionalInfo" to ppAdditionalInfo,
        "preferencePriority" to preferencePriority.toString(),
      ),
    )
    assertThat(firstEvent.agent.role).isEqualTo(PRISON_CA_AGENT.role)
    assertThat(firstEvent.agent.fullName).isEqualTo(PRISON_CA_AGENT.fullName)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/a-standard-address-check-request.sql",
  )
  fun `should delete address check request`() {
    val prisonNumber = "A1234AA"
    val requestId = 1L
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)

    addressService.deleteAddressCheckRequest(prisonNumber, requestId)

    val deletedRequest = curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
    assertThat(deletedRequest).isNull()

    val assessmentEvents = curfewAddressCheckRequest?.assessment?.id?.let {
      assessmentEventRepository.findByAssessmentId(
        it,
      )
    }
    assertThat(assessmentEvents).isNotEmpty
    assertThat(assessmentEvents).hasSize(1)

    val firstEvent = assessmentEvents?.first() as GenericChangedEvent
    assertThat(firstEvent.eventType).isEqualTo(AssessmentEventType.ADDRESS_UPDATED)
    assertThat(firstEvent.summary).isEqualTo("generic change event with type: ADDRESS_UPDATED")
    assertThat(firstEvent.changes["deleteAddressCheckRequestId"]).isEqualTo(requestId.toInt())
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

    val residentSummary = addressService.addResidents(TestData.PRISON_NUMBER, standardAddressCheckRequest.id, listOf(addMainResident, addOtherResident1, addOtherResident2), PRISON_CA_AGENT.toEntity())
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

    // Verify that the event was recorded
    val assessmentEvents = assessmentEventRepository.findByAssessmentId(standardAddressCheckRequest.assessment.id)
    assertThat(assessmentEvents).isNotEmpty
    assertThat(assessmentEvents).hasSize(1)

    val firstEvent = assessmentEvents.first() as GenericChangedEvent
    assertThat(firstEvent.eventType).isEqualTo(AssessmentEventType.RESIDENT_UPDATED)
    assertThat(firstEvent.summary).isEqualTo("generic change event with type: RESIDENT_UPDATED")
    assertThat(firstEvent.changes["newResidents"]).isEqualTo(
      listOf(
        mapOf(
          "residentId" to addMainResident.residentId?.toInt(),
          "forename" to addMainResident.forename,
          "surname" to addMainResident.surname,
          "phoneNumber" to addMainResident.phoneNumber,
          "relation" to addMainResident.relation,
          "dateOfBirth" to addMainResident.dateOfBirth.toString(),
          "age" to addMainResident.age,
          "isMainResident" to addMainResident.isMainResident,
          "isOffender" to addMainResident.isOffender,
        ),
        mapOf(
          "residentId" to addOtherResident1.residentId?.toInt(),
          "forename" to addOtherResident1.forename,
          "surname" to addOtherResident1.surname,
          "phoneNumber" to addOtherResident1.phoneNumber,
          "relation" to addOtherResident1.relation,
          "dateOfBirth" to addOtherResident1.dateOfBirth.toString(),
          "age" to addOtherResident1.age,
          "isMainResident" to addOtherResident1.isMainResident,
          "isOffender" to addOtherResident1.isOffender,
        ),
        mapOf(
          "residentId" to addOtherResident2.residentId?.toInt(),
          "forename" to addOtherResident2.forename,
          "surname" to addOtherResident2.surname,
          "phoneNumber" to addOtherResident2.phoneNumber,
          "relation" to addOtherResident2.relation,
          "dateOfBirth" to addOtherResident2.dateOfBirth.toString(),
          "age" to addOtherResident2.age,
          "isMainResident" to addOtherResident2.isMainResident,
          "isOffender" to addOtherResident2.isOffender,
        ),
      ),
    )
    assertThat(firstEvent.agent.role).isEqualTo(PRISON_CA_AGENT.role)
    assertThat(firstEvent.agent.fullName).isEqualTo(PRISON_CA_AGENT.fullName)
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

    val residentSummary = addressService.addResidents(TestData.PRISON_NUMBER, standardAddressCheckRequest.id, listOf(addResidentRequest), PRISON_CA_AGENT.toEntity())
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

    assertThrows<ItemNotFoundException> { addressService.addResidents(prisonNumber, standardAddressCheckRequest.id, listOf(addResidentRequest), PRISON_CA_AGENT.toEntity()) }
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/a-standard-address-check-request.sql",
  )
  fun `should update case admin additional information`() {
    val prisonNumber = "A1234AA"
    val requestId = 1L
    val additionalInformation = "Updated case admin info"
    val caseAdminInfoRequest = UpdateCaseAdminAdditionInfoRequest(additionalInformation)

    addressService.updateCaseAdminAdditionalInformation(prisonNumber, requestId, caseAdminInfoRequest, PRISON_CA_AGENT.toEntity())

    val curfewAddressCheckRequest = curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
    assertThat(curfewAddressCheckRequest).isNotNull
    assertThat(curfewAddressCheckRequest?.caAdditionalInfo).isEqualTo(additionalInformation)

    val assessmentEvents = curfewAddressCheckRequest?.assessment?.id?.let {
      assessmentEventRepository.findByAssessmentId(it)
    }
    assertThat(assessmentEvents).isNotEmpty
    assertThat(assessmentEvents).hasSize(1)

    val firstEvent = assessmentEvents?.first() as GenericChangedEvent
    assertThat(firstEvent.eventType).isEqualTo(AssessmentEventType.ADDRESS_UPDATED)
    assertThat(firstEvent.changes["caseAdminAdditionalInformation"]).isEqualTo(additionalInformation)
    assertThat(firstEvent.agent.role).isEqualTo(PRISON_CA_AGENT.role)
    assertThat(firstEvent.agent.fullName).isEqualTo(PRISON_CA_AGENT.fullName)
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
