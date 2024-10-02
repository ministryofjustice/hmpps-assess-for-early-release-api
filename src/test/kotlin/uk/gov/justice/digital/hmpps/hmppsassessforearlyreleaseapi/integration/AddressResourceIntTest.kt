package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.StandardAddressCheckRequestSummary

private const val POSTCODE = "SW1X9AH"
private const val UPRN = "200010019924"
private const val PRISON_NUMBER = "A1234AD"
private const val GET_ADDRESSES_FOR_POSTCODE_URL = "/addresses?postcode=$POSTCODE"
private const val GET_ADDRESS_FOR_UPRN_URL = "/address/uprn/$UPRN"
private const val ADD_STANDARD_ADDRESS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/standard-address-check-request"
private const val ADD_CAS_CHECK_REQUEST_URL = "/offender/$PRISON_NUMBER/current-assessment/cas-check-request"

class AddressResourceIntTest : SqsIntegrationTestBase() {

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
    private val priority = AddressPreferencePriority.THIRD
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
  inner class AddCasCheckRequestTests {
    private val caInfo = "ca info"
    private val ppInfo = "pp info"
    private val priority = AddressPreferencePriority.THIRD

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
    fun `should add a standard address check request`() {
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

  private companion object {
    val osPlacesMockServer = OsPlacesMockServer("os-places-api-key")

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
