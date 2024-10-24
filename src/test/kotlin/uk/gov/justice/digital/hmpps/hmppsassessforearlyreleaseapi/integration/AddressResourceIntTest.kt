package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressCheckRequestStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import java.time.LocalDate

private const val POSTCODE = "SW1X9AH"
private const val UPRN = "200010019924"
private const val PRISON_NUMBER = TestData.PRISON_NUMBER
private const val ADDRESS_REQUEST_ID = 1L
private const val GET_ADDRESSES_FOR_POSTCODE_URL = "/addresses?postcode=$POSTCODE"
private const val GET_ADDRESS_FOR_UPRN_URL = "/address/uprn/$UPRN"
private const val ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request"
private const val GET_STANDARD_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request/$ADDRESS_REQUEST_ID"
private const val ADD_CAS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/cas-check-request"
private const val DELETE_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/address-request/$ADDRESS_REQUEST_ID"
private const val GET_ADDRESS_CHECK_REQUESTS_FOR_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/address-check-requests"
private const val ADD_RESIDENT_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request/$ADDRESS_REQUEST_ID/resident"

class AddressResourceIntTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository

  @Nested
  inner class GetAddressesForPostcode {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ADDRESSES_FOR_POSTCODE_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ADDRESSES_FOR_POSTCODE_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ADDRESSES_FOR_POSTCODE_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return addresses with a postcode`() {
      osPlacesMockServer.stubGetAddressesForPostcode(POSTCODE)

      val addresses = webTestClient.get()
        .uri(GET_ADDRESSES_FOR_POSTCODE_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(object : ParameterizedTypeReference<List<AddressSummary>>() {})
        .returnResult().responseBody!!

      assertThat(addresses).hasSize(3)
      assertThat(addresses.map { it.postcode }).containsOnly(POSTCODE)
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
        .bodyValue(anAddStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation())
        .bodyValue(anAddStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(anAddStandardAddressCheckRequest())
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
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(anAddStandardAddressCheckRequest())
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(object : ParameterizedTypeReference<StandardAddressCheckRequestSummary>() {})
        .returnResult().responseBody!!

      assertThat(addressCheckRequest.caAdditionalInfo).isEqualTo(caInfo)
      assertThat(addressCheckRequest.ppAdditionalInfo).isEqualTo(ppInfo)
      assertThat(addressCheckRequest.address.uprn).isEqualTo(uprn)
    }

    private fun anAddStandardAddressCheckRequest(): AddStandardAddressCheckRequest =
      AddStandardAddressCheckRequest(caInfo, ppInfo, priority, uprn)
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
    )
    @Test
    fun `should get a standard address check request`() {
      val addressCheckRequest = webTestClient.get()
        .uri(GET_STANDARD_ADDRESS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(object : ParameterizedTypeReference<StandardAddressCheckRequestSummary>() {})
        .returnResult().responseBody!!

      assertThat(addressCheckRequest.requestId).isEqualTo(ADDRESS_REQUEST_ID)
      assertThat(addressCheckRequest.preferencePriority).isEqualTo(AddressPreferencePriority.FIRST)
      assertThat(addressCheckRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
      assertThat(addressCheckRequest.address.firstLine).isEqualTo("4 ADANAC DRIVE")
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
        .bodyValue(aAddCasCheckRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(ADD_CAS_CHECK_REQUEST_URL)
        .headers(setAuthorisation())
        .bodyValue(aAddCasCheckRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(ADD_CAS_CHECK_REQUEST_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(aAddCasCheckRequest())
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
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(aAddCasCheckRequest())
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(object : ParameterizedTypeReference<CasCheckRequestSummary>() {})
        .returnResult().responseBody!!

      assertThat(addressCheckRequest.caAdditionalInfo).isEqualTo(caInfo)
      assertThat(addressCheckRequest.ppAdditionalInfo).isEqualTo(ppInfo)
      assertThat(addressCheckRequest.preferencePriority).isEqualTo(priority)
    }

    private fun aAddCasCheckRequest(): AddCasCheckRequest =
      AddCasCheckRequest(caInfo, ppInfo, priority)
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
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
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
        .expectBody(object : ParameterizedTypeReference<List<CheckRequestSummary>>() {})
        .returnResult().responseBody!!

      assertThat(checkRequestSummaries).hasSize(1)
      val checkRequest = checkRequestSummaries.first()
      assertThat(checkRequest.requestId).isEqualTo(ADDRESS_REQUEST_ID)
      assertThat(checkRequest.preferencePriority).isEqualTo(AddressPreferencePriority.FIRST)
      assertThat(checkRequest.status).isEqualTo(AddressCheckRequestStatus.IN_PROGRESS)
      assertThat(checkRequest).isInstanceOf(StandardAddressCheckRequestSummary::class.java)

      val standardAddressCheckRequestSummary = checkRequest as StandardAddressCheckRequestSummary
      assertThat(standardAddressCheckRequestSummary.address.firstLine).isEqualTo("4 ADANAC DRIVE")
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
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(anAddResidentRequest())
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(object : ParameterizedTypeReference<ResidentSummary>() {})
        .returnResult().responseBody!!

      assertThat(residentSummary.forename).isEqualTo(forename)
      assertThat(residentSummary.surname).isEqualTo(surname)
      assertThat(residentSummary.phoneNumber).isEqualTo(phoneNumber)
      assertThat(residentSummary.relation).isEqualTo(relation)
      assertThat(residentSummary.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(residentSummary.isMainResident).isEqualTo(isMainResident)
    }

    private fun anAddResidentRequest(): AddResidentRequest =
      AddResidentRequest(forename, surname, phoneNumber, relation, dateOfBirth, age = 47, isMainResident = true)
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
