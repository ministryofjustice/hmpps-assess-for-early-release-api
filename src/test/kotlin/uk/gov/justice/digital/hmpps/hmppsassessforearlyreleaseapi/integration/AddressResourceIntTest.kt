package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressDeleteReasonDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.UpdateCaseAdminAdditionInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.AddressDeleteReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHolder
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ADDRESS_REQUEST_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference
import java.time.LocalDate

private const val POSTCODE = "SW1X9AH"
private const val UPRN = "200010019924"
private const val SEARCH_FOR_ADDRESSES_URL = "/addresses/search/$POSTCODE"
private const val GET_ADDRESS_FOR_UPRN_URL = "/address/uprn/$UPRN"
private const val ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request"
private const val GET_STANDARD_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request/$ADDRESS_REQUEST_ID"
private const val ADD_CAS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/cas-check-request"
private const val DELETE_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/address-request/$ADDRESS_REQUEST_ID"
private const val GET_ADDRESS_CHECK_REQUESTS_FOR_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/address-check-requests"
private const val ADD_RESIDENT_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request/$ADDRESS_REQUEST_ID/resident"
private const val UPDATE_CASE_AMIN_ADDITIONAL_INFO = "/offender/$PRISON_NUMBER/current-assessment/address-request/$ADDRESS_REQUEST_ID/case-admin-additional-information"
private const val ADDRESS_DELETE_REASON = "/offender/$PRISON_NUMBER/current-assessment/withdraw-address/$ADDRESS_REQUEST_ID"

class AddressResourceIntTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository

  @Nested
  inner class SearchForAddresses {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(SEARCH_FOR_ADDRESSES_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(SEARCH_FOR_ADDRESSES_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(SEARCH_FOR_ADDRESSES_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return addresses with given search text`() {
      // Given
      osPlacesMockServer.stubSearchForAddresses(POSTCODE)

      // When
      val result = webTestClient.get()
        .uri(SEARCH_FOR_ADDRESSES_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()

      // Then

      result.expectStatus().isOk
      val addresses = result.expectBody(typeReference<List<AddressSummary>>()).returnResult().responseBody!!
      assertThat(addresses).hasSize(3)
      assertThat(addresses.map { it.postcode }).containsOnly("TEST")
    }
  }

  @Nested
  inner class GetAddressForUprn {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ADDRESS_FOR_UPRN_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ADDRESS_FOR_UPRN_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ADDRESS_FOR_UPRN_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an address for an UPRN`() {
      osPlacesMockServer.stubGetAddressByUprn(UPRN)

      val address = webTestClient.get()
        .uri(GET_ADDRESS_FOR_UPRN_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(AddressSummary::class.java)
        .returnResult().responseBody!!
      assertThat(address.uprn).isEqualTo(UPRN)
    }
  }

  @Nested
  inner class AddStandardAddressCheckRequestTests {
    private val caInfo = "ca info"
    private val ppInfo = "pp info"
    private val priority = AddressPreferencePriority.FIRST
    private val uprn = "200010019924"

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .bodyValue(addStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation())
        .bodyValue(addStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(addStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
      "classpath:test_data/an-address.sql",
    )
    @Test
    fun `should add a standard address check request`() {
      val addressCheckRequest = webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PRISON_CA_AGENT))
        .bodyValue(addStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(typeReference<StandardAddressCheckRequestSummary>())
        .returnResult().responseBody!!

      assertThat(addressCheckRequest.caAdditionalInfo).isEqualTo(caInfo)
      assertThat(addressCheckRequest.ppAdditionalInfo).isEqualTo(ppInfo)
      assertThat(addressCheckRequest.address.uprn).isEqualTo(uprn)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
      "classpath:test_data/an-address.sql",
    )
    @Test
    fun `should return bad request if agent is missing`() {
      webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(addStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    private fun addStandardAddressCheckRequest() = AddStandardAddressCheckRequest(caInfo, ppInfo, priority, uprn)
  }

  @Nested
  inner class GetStandardAddressCheckRequestTests {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
      "classpath:test_data/an-resident.sql",
    )
    @Test
    fun `should get a standard address check request`() {
      val addressCheckRequest = webTestClient.get()
        .uri(GET_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(typeReference<StandardAddressCheckRequestSummary>())
        .returnResult().responseBody!!

      assertThat(addressCheckRequest.requestId).isEqualTo(ADDRESS_REQUEST_ID)
      assertThat(addressCheckRequest.preferencePriority).isEqualTo(AddressPreferencePriority.FIRST)
      assertThat(addressCheckRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
      assertThat(addressCheckRequest.address.firstLine).isEqualTo("1 TEST STREET")
      assertThat(addressCheckRequest.residents).hasSize(3)
      assertThat(addressCheckRequest.residents.first().residentId).isEqualTo(2)
    }
  }

  @Nested
  inner class AddCasCheckRequestTests {
    private val caInfo = "ca info"
    private val ppInfo = "pp info"
    private val priority = AddressPreferencePriority.SECOND

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri(ADD_CAS_CHECK_REQUEST_URL)
        .bodyValue(addCasCheckRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(ADD_CAS_CHECK_REQUEST_URL)
        .headers(setAuthorisation())
        .bodyValue(addCasCheckRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(ADD_CAS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(addCasCheckRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
      "classpath:test_data/an-address.sql",
    )
    @Test
    fun `should add a CAS check request`() {
      val addressCheckRequest = webTestClient.post()
        .uri(ADD_CAS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PRISON_CA_AGENT))
        .bodyValue(addCasCheckRequest())
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(typeReference<CasCheckRequestSummary>())
        .returnResult().responseBody!!

      assertThat(addressCheckRequest.caAdditionalInfo).isEqualTo(caInfo)
      assertThat(addressCheckRequest.ppAdditionalInfo).isEqualTo(ppInfo)
      assertThat(addressCheckRequest.preferencePriority).isEqualTo(priority)
    }

    private fun addCasCheckRequest() = AddCasCheckRequest(caInfo, ppInfo, priority)
  }

  @Nested
  inner class DeleteAddressCheckRequestTests {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.delete()
        .uri(DELETE_ADDRESS_CHECK_REQUEST_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.delete()
        .uri(DELETE_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.delete()
        .uri(DELETE_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should delete an address check request`() {
      webTestClient.delete()
        .uri(DELETE_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PRISON_CA_AGENT))
        .exchange()
        .expectStatus()
        .isNoContent

      assertThat(curfewAddressCheckRequestRepository.findAll()).isEmpty()
    }
  }

  @Nested
  inner class GetCheckRequestsForAssessmentTests {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ADDRESS_CHECK_REQUESTS_FOR_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ADDRESS_CHECK_REQUESTS_FOR_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ADDRESS_CHECK_REQUESTS_FOR_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
      "classpath:test_data/an-resident.sql",
    )
    @Test
    fun `should get check requests for an assessment`() {
      prisonRegisterMockServer.stubGetPrisons()

      val checkRequestSummaries = webTestClient.get()
        .uri(GET_ADDRESS_CHECK_REQUESTS_FOR_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(typeReference<List<CheckRequestSummary>>())
        .returnResult().responseBody!!

      assertThat(checkRequestSummaries).hasSize(1)
      val checkRequest = checkRequestSummaries.first()
      assertThat(checkRequest.requestId).isEqualTo(ADDRESS_REQUEST_ID)
      assertThat(checkRequest.preferencePriority).isEqualTo(AddressPreferencePriority.FIRST)
      assertThat(checkRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
      assertThat(checkRequest).isInstanceOf(StandardAddressCheckRequestSummary::class.java)

      val standardAddressCheckRequestSummary = checkRequest as StandardAddressCheckRequestSummary
      assertThat(standardAddressCheckRequestSummary.address.firstLine).isEqualTo("1 TEST STREET")
      val residentSummary = standardAddressCheckRequestSummary.residents
      assertThat(residentSummary).hasSize(3)
      assertThat(residentSummary.first().residentId).isEqualTo(2)
    }
  }

  @Nested
  inner class AddResidentTests {
    private val forename = "Refugio"
    private val surname = "Whittaker"
    private val phoneNumber = "07634183674"
    private val relation = "Daughter"
    private val dateOfBirth = LocalDate.of(1983, 6, 28)
    private val isMainResident = true

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri(ADD_RESIDENT_URL)
        .bodyValue(anAddResidentRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(ADD_RESIDENT_URL)
        .headers(setAuthorisation())
        .bodyValue(anAddResidentRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(ADD_RESIDENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(anAddResidentRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should add a standard address check request`() {
      val residentSummary = webTestClient.post()
        .uri(ADD_RESIDENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PRISON_CA_AGENT))
        .bodyValue(anAddResidentRequest())
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(typeReference<List<ResidentSummary>>())
        .returnResult().responseBody!!

      assertThat(residentSummary.first().forename).isEqualTo(forename)
      assertThat(residentSummary.first().surname).isEqualTo(surname)
      assertThat(residentSummary.first().phoneNumber).isEqualTo(phoneNumber)
      assertThat(residentSummary.first().relation).isNull()
      assertThat(residentSummary.first().dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(residentSummary.first().isMainResident).isEqualTo(isMainResident)
      assertThat(residentSummary.last().isMainResident).isFalse()
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should return 400 BAD_REQUEST for validation failure`() {
      val invalidAddResidentRequest = listOf(
        AddResidentRequest(
          residentId = null,
          forename = "Kadra",
          surname = "Semparri",
          phoneNumber = "07739754284",
          relation = null,
          dateOfBirth = LocalDate.now().minusYears(30),
          age = 30,
          isMainResident = false,
          isOffender = false,
        ),
      )

      webTestClient.post()
        .uri(ADD_RESIDENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(invalidAddResidentRequest)
        .exchange()
        .expectStatus()
        .isEqualTo(400)
        .expectBody()
        .jsonPath("$.userMessage").isEqualTo("Validation failure: 400 BAD_REQUEST \"Validation failure\"")
    }

    private fun anAddResidentRequest(): List<AddResidentRequest> = listOf(
      AddResidentRequest(1, forename, surname, phoneNumber, null, dateOfBirth, age = 47, isMainResident = true, isOffender = true),
      AddResidentRequest(2, forename, surname, phoneNumber, relation, dateOfBirth, age = 37, isMainResident = false, isOffender = false),
    )
  }

  @Nested
  inner class UpdateCaseAdminAdditionalInformation {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(UPDATE_CASE_AMIN_ADDITIONAL_INFO)
        .bodyValue(anUpdateCaAdditionalInfoRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(UPDATE_CASE_AMIN_ADDITIONAL_INFO)
        .headers(setAuthorisation())
        .bodyValue(anUpdateCaAdditionalInfoRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(UPDATE_CASE_AMIN_ADDITIONAL_INFO)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(anUpdateCaAdditionalInfoRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should update case admin additional information`() {
      val agentHolder = AgentHolder()
      agentHolder.agent = PRISON_CA_AGENT
      webTestClient.put()
        .uri(UPDATE_CASE_AMIN_ADDITIONAL_INFO)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PRISON_CA_AGENT))
        .bodyValue(anUpdateCaAdditionalInfoRequest())
        .exchange()
        .expectStatus()
        .isNoContent

      val addressCheckRequest = curfewAddressCheckRequestRepository.findByIdOrNull(ADDRESS_REQUEST_ID)
      assertThat(addressCheckRequest?.caAdditionalInfo).isEqualTo(anUpdateCaAdditionalInfoRequest().additionalInformation)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should return bad request for additional info longer than 1000 characters`() {
      val updateCaAdditionalInfoRequest = UpdateCaseAdminAdditionInfoRequest("A".repeat(1001))

      webTestClient.put()
        .uri(UPDATE_CASE_AMIN_ADDITIONAL_INFO)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(updateCaAdditionalInfoRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    private fun anUpdateCaAdditionalInfoRequest() = UpdateCaseAdminAdditionInfoRequest("some information")
  }

  @Nested
  inner class AddressDeleteReasonTests {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(ADDRESS_DELETE_REASON)
        .bodyValue(anAddressDeleteReasonRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(ADDRESS_DELETE_REASON)
        .headers(setAuthorisation())
        .bodyValue(anAddressDeleteReasonRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(ADDRESS_DELETE_REASON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(anAddressDeleteReasonRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should update curfew address with address deletion reason`() {
      webTestClient.put()
        .uri(ADDRESS_DELETE_REASON)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN"), agent = PRISON_CA_AGENT))
        .bodyValue(anAddressDeleteReasonRequest())
        .exchange()
        .expectStatus()
        .isNoContent

      val addressCheckRequest = curfewAddressCheckRequestRepository.findByIdOrNull(ADDRESS_REQUEST_ID)
      assertThat(addressCheckRequest?.addressDeletionEvent?.reasonType).isEqualTo(anAddressDeleteReasonRequest().addressDeleteReasonType)
      assertThat(addressCheckRequest?.addressDeletionEvent?.otherReason).isEqualTo(anAddressDeleteReasonRequest().addressDeleteOtherReason)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should return bad request for null address delete other reason if reason type is other reason`() {
      val updateAddressDeleteReasonRequest = AddressDeleteReasonDto(AddressDeleteReasonType.OTHER_REASON, null)

      webTestClient.put()
        .uri(ADDRESS_DELETE_REASON)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(updateAddressDeleteReasonRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    private fun anAddressDeleteReasonRequest() = AddressDeleteReasonDto(AddressDeleteReasonType.OTHER_REASON, "other reason")
  }

  private companion object {
    val osPlacesMockServer = OsPlacesMockServer(OS_API_KEY)
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      osPlacesMockServer.start()
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      osPlacesMockServer.stop()
      prisonRegisterMockServer.stop()
    }
  }
}
