package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiDPA

@Service
class AddressService(
  private val osPlacesApiClient: OsPlacesApiClient,
) {
  fun getAddressesForPostcode(postcode: String): List<OsPlacesApiDPA> {
    return osPlacesApiClient.getAddressesForPostcode(postcode)
  }

  fun getAddressForUprn(uprn: String): OsPlacesApiDPA {
    return osPlacesApiClient.getAddressForUprn(uprn)
  }
}
