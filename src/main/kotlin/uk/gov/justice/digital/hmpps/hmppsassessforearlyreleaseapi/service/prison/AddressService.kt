package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiAddress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient

@Service
class AddressService(
  private val osPlacesApiClient: OsPlacesApiClient,
) {
  fun getAddressesForPostcode(postcode: String): List<OsPlacesApiAddress> {
    return osPlacesApiClient.getAddressesForPostcode(postcode)
  }

//  fun getAddressForUprn(urpn: String): DPA {
//    return osPlacesApiClient.getAddressForUprn(urpn)
//  }
}
