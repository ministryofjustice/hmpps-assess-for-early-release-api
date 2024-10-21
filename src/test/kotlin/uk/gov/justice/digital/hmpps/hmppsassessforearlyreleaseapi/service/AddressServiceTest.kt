package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.AddressService
import java.util.Optional

class AddressServiceTest {
  private val addressRepository = mock<AddressRepository>()
  private val casCheckRequestRepository = mock<CasCheckRequestRepository>()
  private val curfewAddressCheckRequestRepository = mock<CurfewAddressCheckRequestRepository>()
  private val offenderRepository = mock<OffenderRepository>()
  private val osPlacesApiClient = mock<OsPlacesApiClient>()
  private val residentRepository = mock<ResidentRepository>()
  private val standardAddressCheckRequestRepository = mock<StandardAddressCheckRequestRepository>()

  private val addressService = AddressService(
    addressRepository,
    casCheckRequestRepository,
    curfewAddressCheckRequestRepository,
    offenderRepository,
    osPlacesApiClient,
    standardAddressCheckRequestRepository,
    residentRepository,
  )

  @Test
  fun `should delete a address check request`() {
    val addressCheckRequest = aStandardAddressCheckRequest()
    val prisonNumber = addressCheckRequest.assessment.offender.prisonNumber
    val requestId = aStandardAddressCheckRequest().id

    whenever(curfewAddressCheckRequestRepository.findById(requestId)).thenReturn(Optional.of(addressCheckRequest))

    addressService.deleteAddressCheckRequest(prisonNumber, requestId)

    verify(curfewAddressCheckRequestRepository).findById(requestId)
    verify(curfewAddressCheckRequestRepository).delete(addressCheckRequest)
  }

  @Test
  fun `should not delete an address request that is not linked to the offender`() {
    val addressCheckRequest = aStandardAddressCheckRequest()
    val prisonNumber = "invalid-prison-number"
    val requestId = aStandardAddressCheckRequest().id

    whenever(curfewAddressCheckRequestRepository.findById(requestId)).thenReturn(Optional.of(addressCheckRequest))

    val exception = assertThrows<HttpClientErrorException> { addressService.deleteAddressCheckRequest(prisonNumber, requestId) }
    assertThat(exception.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }
}
