package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.kotlin.verify
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.GotenbergMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum.DocumentSubjectType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class DocumentResourceIntTest : SqsIntegrationTestBase() {

  private val gotenbergMockServer = GotenbergMockServer()
  private val prisonerSearchApiMockServer = PrisonerSearchMockServer()
  private val prisonRegisterMockServer = PrisonRegisterMockServer()

  private val pdfBytes = "PDF_BYTES".toByteArray()
  private val prisonNumber = "A1234AA"

  @Captor
  lateinit var stringArgumentCaptor: ArgumentCaptor<String>

  @BeforeEach
  fun startMocks() {
    gotenbergMockServer.start()
    prisonerSearchApiMockServer.start()
    prisonRegisterMockServer.start()
  }

  @AfterEach
  fun stopMocks() {
    gotenbergMockServer.stop()
    prisonerSearchApiMockServer.stop()
    prisonRegisterMockServer.stop()
  }

  @ParameterizedTest
  @EnumSource(
    value = DocumentSubjectType::class,
    names = ["OFFENDER_ELIGIBLE_FORM", "OFFENDER_ADDRESS_CHECKS_INFORMATION_FORM"],
  )
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  fun `get documents by prison number and document subject type should process correctly`(
    documentSubjectType: DocumentSubjectType,
  ) {
    // Given
    val getOffenderDocumentUrl = "/offender/$prisonNumber/document/$documentSubjectType"

    gotenbergMockServer.stubPostPdf(pdfBytes)
    prisonRegisterMockServer.stubGetPrisons()
    stubPrisonerSearch(prisonNumber)

    // When
    val responseSpec = doGetRequestDocument(getOffenderDocumentUrl)

    // Then
    responseSpec.expectStatus().isOk

    val result = responseSpec.expectBody(ByteArray::class.java)
      .returnResult().responseBody
    assertThat(result).isEqualTo(pdfBytes)
    assertDocument(documentSubjectType)
  }

  @Test
  fun `should return unauthorized if no token`() {
    // Given
    val documentSubjectType = DocumentSubjectType.OFFENDER_APPROVED_FORM
    val url = "/offender/$prisonNumber/document/$documentSubjectType"

    // When
    val responseSpec = webTestClient.get()
      .uri(url)
      .exchange()

    // Then
    responseSpec.expectStatus().isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    // Given

    val documentSubjectType = DocumentSubjectType.OFFENDER_APPROVED_FORM
    val url = "/offender/$prisonNumber/document/$documentSubjectType"

    // When
    val responseSpec = webTestClient.get()
      .uri(url)
      .headers(setAuthorisation())
      .exchange()

    // Then
    responseSpec.expectStatus().isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    // Given
    val documentSubjectType = DocumentSubjectType.OFFENDER_APPROVED_FORM
    val url = "/offender/$prisonNumber/document/$documentSubjectType"

    // When
    val responseSpec = webTestClient.get()
      .uri(url)
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()

    // Then
    responseSpec.expectStatus().isForbidden
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  @Test
  fun `should handle 500 error`() {
    // Given
    val documentSubjectType = DocumentSubjectType.OFFENDER_ELIGIBLE_FORM
    val url = "/offender/$prisonNumber/document/$documentSubjectType"
    val expectedErrorMessage = "500 Internal Server Error from POST http://localhost:3001/forms/chromium/convert/html"
    val expectedHttpErrorCode = 500

    gotenbergMockServer.stubPostPdfBadRequest()
    prisonRegisterMockServer.stubGetPrisons()
    stubPrisonerSearch(prisonNumber)

    // When
    val responseSpec = doGetRequestDocument(url)

    // Then
    assertError(responseSpec, expectedHttpErrorCode, expectedErrorMessage)
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  @Test
  fun `should handle no prison found error`() {
    // Given
    val documentSubjectType = DocumentSubjectType.OFFENDER_ELIGIBLE_FORM
    val url = "/offender/$prisonNumber/document/$documentSubjectType"
    val expectedErrorMessage = "ItemNotFoundException: Cannot find a prison with prison id in prison register: BMI"
    val expectedHttpErrorCode = 404

    gotenbergMockServer.stubPostPdf(pdfBytes)
    prisonRegisterMockServer.stubGetPrisonsNoResults()
    stubPrisonerSearch(prisonNumber)

    // When
    val responseSpec = doGetRequestDocument(url)

    // Then
    assertError(responseSpec, expectedHttpErrorCode, expectedErrorMessage)
  }

  @Sql(
    "classpath:test_data/reset.sql",
  )
  @Test
  fun `should handle Offender not found error`() {
    // Given
    val documentSubjectType = DocumentSubjectType.OFFENDER_ELIGIBLE_FORM
    val url = "/offender/$prisonNumber/document/$documentSubjectType"
    val expectedErrorMessage = "ItemNotFoundException: Cannot find offender with prisonNumber $prisonNumber"
    val expectedHttpErrorCode = 404

    gotenbergMockServer.stubPostPdf(pdfBytes)
    prisonRegisterMockServer.stubGetPrisons()
    stubPrisonerSearch(prisonNumber)

    // When
    val responseSpec = doGetRequestDocument(url)

    // Then
    assertError(responseSpec, expectedHttpErrorCode, expectedErrorMessage)
  }

  private fun assertError(
    responseSpec: WebTestClient.ResponseSpec,
    expectedHttpErrorCode: Int,
    expectedErrorMessage: String,
  ) {
    responseSpec.expectStatus().isEqualTo(expectedHttpErrorCode)
      .expectBody()
      .jsonPath("$.developerMessage").isEqualTo(expectedErrorMessage)
  }

  private fun assertDocument(documentSubjectType: DocumentSubjectType) {
    assertThat(getThymeleafHtml()).isEqualToIgnoringWhitespace(getExpectedThymeleafHtml(documentSubjectType))
  }

  private fun stubPrisonerSearch(prisonNumber: String) {
    prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
      objectMapper.writeValueAsString(
        listOf(
          PrisonerSearchPrisoner(
            bookingId = "123",
            prisonerNumber = prisonNumber,
            prisonId = "HMI",
            firstName = "FIRST-1",
            lastName = "LAST-1",
            dateOfBirth = LocalDate.of(1981, 5, 23),
            homeDetentionCurfewEligibilityDate = LocalDate.of(2025, 2, 23),
            sentenceStartDate = LocalDate.of(2025, 2, 23),
            cellLocation = "A-1-002",
            mostSeriousOffence = "Robbery",
            prisonName = "Birmingham (HMP)",
          ),
        ),
      ),
    )
  }

  private fun doGetRequestDocument(getOffenderDocumentUrl: String): WebTestClient.ResponseSpec = webTestClient.get()
    .uri(getOffenderDocumentUrl)
    .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
    .exchange()

  private fun getThymeleafHtml(): String? {
    verify(gotenbergApiClient).sendCreatePdfRequest(capture(stringArgumentCaptor))
    return stringArgumentCaptor.value
  }

  private fun getExpectedThymeleafHtml(
    documentSubjectType: DocumentSubjectType,
    fileType: String? = "html",
    dir: String? = "/test_data/thymeleaf_html",
  ): String {
    val name = documentSubjectType.name.lowercase()

    return this.javaClass.getResourceAsStream("$dir/$name.$fileType")!!.bufferedReader(
      StandardCharsets.UTF_8,
    ).readText()
  }
}
