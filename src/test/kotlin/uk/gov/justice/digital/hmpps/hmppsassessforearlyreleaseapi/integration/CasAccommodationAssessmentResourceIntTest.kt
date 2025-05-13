package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.OsPlacesMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.AddPrisonerEligibilityInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.Cas2ReferralInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentAddressRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentOutcomeRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentTypeRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationStatusInfoResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasOutcomeType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.FlagCasAccommodationAssessmentForReferralRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasAccommodationAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val POST_CAS_REQUEST_URL = "/offender/{prisonNumber}/current-assessment/cas/requested"
private const val PUT_CAS_ELIGIBILITY_REQUEST_URL = "/offender/cas/assessment/{reference}/eligibility"
private const val PUT_CAS_TYPE_REQUEST_URL = "/offender/cas/assessment/{reference}/type"
private const val PUT_CAS_IS_REFERRED_REQUEST_URL = "/offender/cas/assessment/{reference}/is-referred"
private const val PUT_CAS2_REFERRAL_INFO_REQUEST_URL = "/offender/cas/assessment/{reference}/cas-2/add-referral-info"
private const val PUT_CAS_OUTCOME_REQUEST_URL = "/offender/cas/assessment/{reference}/outcome"
private const val PUT_CAS_ADD_ADDRESS_REQUEST_URL = "/offender/cas/assessment/{reference}/address"

class CasAccommodationAssessmentResourceIntTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var casAccommodationAssessmentRepository: CasAccommodationAssessmentRepository

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

  abstract inner class BaseTestClass(private val url: String, val method: HttpMethod) {

    protected abstract fun getDefaultRequestBody(): Any?

    protected fun actionHttpMethod(testUrl: String = url): WebTestClient.RequestBodySpec = when (method) {
      POST -> webTestClient.post().uri(testUrl)
      PUT -> webTestClient.put().uri(testUrl)
      else -> {
        throw IllegalArgumentException("Unsupported method: $method")
      }
    }

    @Test
    fun `should return unauthorized if no token`() {
      val responseSpec = actionHttpMethod()
      getDefaultRequestBody()?.let {
        responseSpec.bodyValue(it)
      }
      responseSpec.exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      val responseSpec = actionHttpMethod()
        .headers(setAuthorisation())
      getDefaultRequestBody()?.let {
        responseSpec.bodyValue(it)
      }
      responseSpec.exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      val responseSpec = actionHttpMethod().headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      getDefaultRequestBody()?.let {
        responseSpec.bodyValue(it)
      }
      responseSpec.exchange()
        .expectStatus()
        .isForbidden
    }
  }

  @Nested
  inner class PostCasRequestedByPerson : BaseTestClass(POST_CAS_REQUEST_URL.replace("{prisonNumber}", "A1234AA"), POST) {

    override fun getDefaultRequestBody(): Any? = null

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should create CAS accommodation assessment when prisoner requests`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .exchange()

      // Then
      result.expectStatus().isCreated
      val casAccommodationStatusInfoResponse = result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.PROPOSED)
      assertThat(casAccommodationStatusInfoResponse.reference).isGreaterThan(0)
    }
  }

  @Nested
  inner class PutEligibilityInfoOnCASAssessment : BaseTestClass(PUT_CAS_ELIGIBILITY_REQUEST_URL.replace("{reference}", "1"), PUT) {

    override fun getDefaultRequestBody(): Any = AddPrisonerEligibilityInfoRequest(false, "Because I can")

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `add eligibility info to CAS accommodation assessment`() {
      // Given
      val reference = 1L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse = result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.PERSON_INELIGIBLE)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)
    }
  }

  @Nested
  inner class PutCasTypeOnCASAssessment : BaseTestClass(PUT_CAS_TYPE_REQUEST_URL.replace("{reference}", "2"), PUT) {

    override fun getDefaultRequestBody(): Any = CasAccommodationAssessmentTypeRequest(CasType.CAS_1)

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `set CAS type for accommodation assessment`() {
      // Given
      val reference = 2L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse = result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.PERSON_ELIGIBLE)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)
    }
  }

  @Nested
  inner class PutCasIsReferredOnCASAssessment : BaseTestClass(PUT_CAS_IS_REFERRED_REQUEST_URL.replace("{reference}", "2"), PUT) {

    override fun getDefaultRequestBody(): Any = FlagCasAccommodationAssessmentForReferralRequest(true)

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `flags CAS accommodation assessment has been referred`() {
      // Given
      val reference = 2L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse = result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.REFERRAL_REQUESTED)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)
    }
  }

  @Nested
  inner class PutCas2ReferralInfoOnCASAssessment : BaseTestClass(PUT_CAS2_REFERRAL_INFO_REQUEST_URL.replace("{reference}", "3"), PUT) {

    override fun getDefaultRequestBody(): Any = Cas2ReferralInfoRequest("areasToAvoidInfo", "supportingInfoForReferral")

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Adds CAS2 referral info to the CAS accommodation assessment`() {
      // Given
      val reference = 3L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse = result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.REFERRAL_REQUESTED)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Does not add CAS2 referral info when the assessment has not been set`() {
      // Given
      val reference = 1L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod(PUT_CAS2_REFERRAL_INFO_REQUEST_URL.replace("{reference}", reference.toString()))
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isBadRequest
      val errorResponse = result.expectBody(ErrorResponse::class.java)
        .returnResult().responseBody
      assertThat(errorResponse).isNotNull
      assertThat(errorResponse!!.developerMessage).isEqualTo("Cas assessment is not a Cas2 assessment type:null")
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Does not add CAS2 referral info when the assessment is CAS1 type`() {
      // Given
      val reference = 4L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod(PUT_CAS2_REFERRAL_INFO_REQUEST_URL.replace("{reference}", reference.toString()))
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isBadRequest
      val errorResponse = result.expectBody(ErrorResponse::class.java)
        .returnResult().responseBody
      assertThat(errorResponse).isNotNull
      assertThat(errorResponse!!.developerMessage).isEqualTo("Cas assessment is not a Cas2 assessment type:CAS_1")
    }
  }

  @Nested
  inner class PutOutcomeOnCASAssessment : BaseTestClass(PUT_CAS_OUTCOME_REQUEST_URL.replace("{reference}", "4"), PUT) {

    override fun getDefaultRequestBody(): Any = CasAccommodationAssessmentOutcomeRequest(CasOutcomeType.REFERRAL_REFUSED)

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Adds referral refused outcome for the CAS accommodation assessment`() {
      // Given
      val reference = 4L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse =
        result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.REFERRAL_REFUSED)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Adds referral withdrawn outcome for the CAS accommodation assessment`() {
      // Given
      val reference = 4L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val request = CasAccommodationAssessmentOutcomeRequest(CasOutcomeType.REFERRAL_WITHDRAWN)

      // When
      val result = actionHttpMethod(PUT_CAS_OUTCOME_REQUEST_URL.replace("{reference}", reference.toString()))
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(request)
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse =
        result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.REFERRAL_WITHDRAWN)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Adds referral accepted refused outcome for the CAS accommodation assessment`() {
      // Given
      val reference = 4L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val request = CasAccommodationAssessmentOutcomeRequest(CasOutcomeType.REFERRAL_ACCEPTED)

      // When
      val result = actionHttpMethod(PUT_CAS_OUTCOME_REQUEST_URL.replace("{reference}", reference.toString()))
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(request)
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse =
        result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.REFERRAL_ACCEPTED)
    }
  }

  @Nested
  inner class AddAddressToCASAssessment : BaseTestClass(PUT_CAS_ADD_ADDRESS_REQUEST_URL.replace("{reference}", "5"), PUT) {

    override fun getDefaultRequestBody(): Any = CasAccommodationAssessmentAddressRequest(
      line1 = "Maengwyn",
      line2 = "Fishguard Rd",
      townOrCity = "Newport",
      postCode = "SA420UQ",
    )

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders-with-cas-assessment.sql",
    )
    @Test
    fun `Add an address for the CAS accommodation assessment`() {
      // Given
      val reference = 5L
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      osPlacesMockServer.stubGetAddressesForPostcode("SA420UQ")

      // When
      val result = actionHttpMethod()
        .headers(setAuthorisation(roles = roles, agent = PROBATION_COM_AGENT))
        .bodyValue(getDefaultRequestBody())
        .exchange()

      // Then
      result.expectStatus().isOk
      val casAccommodationStatusInfoResponse =
        result.expectBody(typeReference<CasAccommodationStatusInfoResponse>()).returnResult().responseBody!!
      assertThat(casAccommodationStatusInfoResponse.status).isEqualTo(CasStatus.ADDRESS_PROVIDED)
      assertThat(casAccommodationStatusInfoResponse.reference).isEqualTo(reference)

      val casAssessment = casAccommodationAssessmentRepository.findById(reference).get()
      assertThat(casAssessment.address).isNotNull
      casAssessment.address?.let {
        assertThat(it.firstLine).isEqualTo("Maengwyn")
        assertThat(it.secondLine).isEqualTo("Fishguard Rd")
        assertThat(it.town).isEqualTo("Newport")
        assertThat(it.county).isEqualTo("WILTSHIRE")
        assertThat(it.postcode).isEqualTo("SA420UQ")
        assertThat(it.country).isEqualTo("England")
        assertThat(it.addressLastUpdated).isEqualTo(LocalDate.now())
        assertThat(it.createdTimestamp).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))
        assertThat(it.lastUpdatedTimestamp).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))
      }
    }
  }
}
