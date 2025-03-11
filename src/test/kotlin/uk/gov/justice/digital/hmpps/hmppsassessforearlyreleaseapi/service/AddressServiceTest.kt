package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CheckRequestType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHolder
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import java.util.Optional

class AddressServiceTest {
  private val addressRepository = mock<AddressRepository>()
  private val assessmentService = mock<AssessmentService>()
  private val casCheckRequestRepository = mock<CasCheckRequestRepository>()
  private val curfewAddressCheckRequestRepository = mock<CurfewAddressCheckRequestRepository>()
  private val offenderRepository = mock<OffenderRepository>()
  private val osPlacesApiClient = mock<OsPlacesApiClient>()
  private val residentRepository = mock<ResidentRepository>()
  private val standardAddressCheckRequestRepository = mock<StandardAddressCheckRequestRepository>()
  private val assessmentRepository = mock<AssessmentRepository>()

  private val addressService = AddressService(
    addressRepository,
    assessmentService,
    casCheckRequestRepository,
    curfewAddressCheckRequestRepository,
    offenderRepository,
    osPlacesApiClient,
    standardAddressCheckRequestRepository,
    residentRepository,
    assessmentRepository,
  )

  @Test
  fun `should get all check requests linked to an assessment`() {
    // Given
    val addressCheckRequest = aStandardAddressCheckRequest()
    val casCheckRequest = aCasCheckRequest()
    val assessment = anOffender().currentAssessment()

    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(assessment)
    whenever(curfewAddressCheckRequestRepository.findByAssessment(assessment)).thenReturn(
      listOf(
        addressCheckRequest,
        casCheckRequest,
      ),
    )

    // When
    val checkRequests = addressService.getCheckRequestsForAssessment(PRISON_NUMBER)

    // Then
    assertThat(checkRequests).hasSize(2)
    val addressCheckRequestSummary = checkRequests.first() as StandardAddressCheckRequestSummary
    assertThat(addressCheckRequestSummary.requestType).isEqualTo(CheckRequestType.STANDARD_ADDRESS)
    assertThat(addressCheckRequestSummary.requestId).isEqualTo(addressCheckRequest.id)
    assertThat(addressCheckRequestSummary.preferencePriority).isEqualTo(addressCheckRequest.preferencePriority)
    assertThat(addressCheckRequestSummary.dateRequested).isEqualTo(addressCheckRequest.dateRequested)
    assertThat(addressCheckRequestSummary.status).isEqualTo(addressCheckRequest.status)
    assertThat(addressCheckRequestSummary.residents).hasSize(1)
    assertThat(addressCheckRequestSummary.residents.first().residentId).isEqualTo(1)

    val casCheckRequestSummary = checkRequests[1]
    assertThat(casCheckRequestSummary.requestType).isEqualTo(CheckRequestType.CAS)
    assertThat(casCheckRequestSummary.requestId).isEqualTo(casCheckRequest.id)
    assertThat(casCheckRequestSummary.preferencePriority).isEqualTo(casCheckRequest.preferencePriority)
    assertThat(casCheckRequestSummary.dateRequested).isEqualTo(casCheckRequest.dateRequested)
    assertThat(casCheckRequestSummary.status).isEqualTo(casCheckRequest.status)
  }

  @Test
  fun `should delete a address check request`() {
    val addressCheckRequest = aStandardAddressCheckRequest()
    val prisonNumber = addressCheckRequest.assessment.offender.prisonNumber
    val requestId = aStandardAddressCheckRequest().id

    whenever(curfewAddressCheckRequestRepository.findById(requestId)).thenReturn(Optional.of(addressCheckRequest))
    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(addressCheckRequest.assessment)

    val agentHolder = AgentHolder()
    agentHolder.agent = PRISON_CA_AGENT

    addressService.deleteAddressCheckRequest(prisonNumber, requestId, agentHolder.getAgentOrThrow())

    verify(curfewAddressCheckRequestRepository).findById(requestId)
    verify(curfewAddressCheckRequestRepository).delete(addressCheckRequest)
  }

  @Test
  fun `should not delete an address request that is not linked to the offender`() {
    val addressCheckRequest = aStandardAddressCheckRequest()
    val prisonNumber = "invalid-prison-number"
    val requestId = aStandardAddressCheckRequest().id

    whenever(curfewAddressCheckRequestRepository.findById(requestId)).thenReturn(Optional.of(addressCheckRequest))

    val agentHolder = AgentHolder()
    agentHolder.agent = PRISON_CA_AGENT

    assertThrows<ItemNotFoundException> { addressService.deleteAddressCheckRequest(prisonNumber, requestId, agentHolder.getAgentOrThrow()) }
  }
}
