package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiDPA

@Service
class AddressService(
  private val addressRepository: AddressRepository,
  private val osPlacesApiClient: OsPlacesApiClient,
) {
  fun getAddressesForPostcode(postcode: String): List<AddressSummary> =
    osPlacesApiClient.getAddressesForPostcode(postcode).map { it.toAddressSummary() }

  fun getAddressForUprn(uprn: String): AddressSummary {
    val existingAddress = addressRepository.findByUprn(uprn)
    if (existingAddress != null) {
      return AddressSummary(
        uprn = existingAddress.uprn,
        firstLine = existingAddress.firstLine,
        secondLine = existingAddress.secondLine,
        town = existingAddress.town,
        county = existingAddress.county,
        postcode = existingAddress.postcode,
        country = existingAddress.country,
        xCoordinate = existingAddress.xCoordinate,
        yCoordinate = existingAddress.yCoordinate,
        addressLastUpdated = existingAddress.addressLastUpdated,
      )
    }

    val osAddress = osPlacesApiClient.getAddressForUprn(uprn)
    addressRepository.save(osAddress.toAddress())
    return osAddress.toAddressSummary()
  }

  private fun OsPlacesApiDPA.toAddressSummary(): AddressSummary =
    AddressSummary(
      uprn = this.uprn,
      firstLine = if (this.buildingNumber != null) this.buildingNumber + " " + this.thoroughfareName else this.thoroughfareName,
      secondLine = this.locality,
      town = this.postTown,
      county = this.county,
      postcode = this.postcode,
      country = this.countryDescription.split("\\s+".toRegex()).last(),
      xCoordinate = this.xCoordinate,
      yCoordinate = this.yCoordinate,
      addressLastUpdated = this.lastUpdateDate,
    )

  private fun OsPlacesApiDPA.toAddress(): Address =
    Address(
      uprn = this.uprn,
      firstLine = if (this.buildingNumber != null) this.buildingNumber + " " + this.thoroughfareName else this.thoroughfareName,
      secondLine = this.locality,
      town = this.postTown,
      county = this.county,
      postcode = this.postcode,
      country = this.countryDescription.split("\\s+".toRegex()).last(),
      xCoordinate = this.xCoordinate,
      yCoordinate = this.yCoordinate,
      addressLastUpdated = this.lastUpdateDate,
    )
}
