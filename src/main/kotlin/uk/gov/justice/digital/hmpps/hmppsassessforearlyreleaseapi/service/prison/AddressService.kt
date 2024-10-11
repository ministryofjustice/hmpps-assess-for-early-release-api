package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiDPA

@Service
class AddressService(
  private val addressRepository: AddressRepository,
  private val casCheckRequestRepository: CasCheckRequestRepository,
  private val offenderRepository: OffenderRepository,
  private val osPlacesApiClient: OsPlacesApiClient,
  private val standardAddressCheckRequestRepository: StandardAddressCheckRequestRepository,
) {
  fun getAddressesForPostcode(postcode: String): List<AddressSummary> =
    osPlacesApiClient.getAddressesForPostcode(postcode).map { it.toAddressSummary() }

  @Transactional
  fun getAddressForUprn(uprn: String): AddressSummary {
    val existingAddress = addressRepository.findByUprn(uprn)
    if (existingAddress != null) {
      return existingAddress.toAddressSummary()
    }

    val osAddress = osPlacesApiClient.getAddressForUprn(uprn)
    addressRepository.save(osAddress.toAddress())
    return osAddress.toAddressSummary()
  }

  @Transactional
  fun addStandardAddressCheckRequest(
    prisonNumber: String,
    addStandardAddressCheckRequest: AddStandardAddressCheckRequest,
  ): StandardAddressCheckRequestSummary {
    val uprn = addStandardAddressCheckRequest.addressUprn
    var address = addressRepository.findByUprn(uprn)
    if (address == null) {
      address = osPlacesApiClient.getAddressForUprn(uprn).toAddress()
      address = addressRepository.save(address)
    }

    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: error("Cannot find offender with prisonNumber $prisonNumber")

    val standardAddressCheckRequest = standardAddressCheckRequestRepository.save(
      StandardAddressCheckRequest(
        caAdditionalInfo = addStandardAddressCheckRequest.caAdditionalInfo,
        ppAdditionalInfo = addStandardAddressCheckRequest.ppAdditionalInfo,
        preferencePriority = addStandardAddressCheckRequest.preferencePriority,
        address = address!!,
        assessment = offender.currentAssessment(),
      ),
    )

    return standardAddressCheckRequest.toSummary()
  }

  @Transactional
  fun addCasCheckRequest(
    prisonNumber: String,
    addCasCheckRequest: AddCasCheckRequest,
  ): CasCheckRequestSummary {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: error("Cannot find offender with prisonNumber $prisonNumber")

    val casCheckRequest = casCheckRequestRepository.save(
      CasCheckRequest(
        caAdditionalInfo = addCasCheckRequest.caAdditionalInfo,
        ppAdditionalInfo = addCasCheckRequest.ppAdditionalInfo,
        preferencePriority = addCasCheckRequest.preferencePriority,
        assessment = offender.currentAssessment(),
      ),
    )
    return casCheckRequest.toSummary()
  }

  private fun OsPlacesApiDPA.toAddressSummary(): AddressSummary =
    AddressSummary(
      uprn = this.uprn,
      firstLine = this.getAddressFirstLine(),
      secondLine = this.locality,
      town = this.postTown,
      county = this.county,
      postcode = this.postcode,
      country = this.countryDescription.split("\\s+".toRegex()).last(),
      xCoordinate = this.xCoordinate,
      yCoordinate = this.yCoordinate,
      addressLastUpdated = this.lastUpdateDate,
    )

  private fun OsPlacesApiDPA.getAddressFirstLine(): String {
    var firstLine = if (this.organisationName != null) this.organisationName + ", " else ""
    firstLine += if (this.buildingName != null) this.buildingName + ", " else ""
    firstLine += if (this.buildingNumber != null) this.buildingNumber + " " + this.thoroughfareName else this.thoroughfareName
    return firstLine
  }

  private fun OsPlacesApiDPA.toAddress(): Address =
    Address(
      uprn = this.uprn,
      firstLine = this.getAddressFirstLine(),
      secondLine = this.locality,
      town = this.postTown,
      county = this.county,
      postcode = this.postcode,
      country = this.countryDescription.split("\\s+".toRegex()).last(),
      xCoordinate = this.xCoordinate,
      yCoordinate = this.yCoordinate,
      addressLastUpdated = this.lastUpdateDate,
    )

  private fun Address.toAddressSummary() = AddressSummary(
    uprn = this.uprn,
    firstLine = this.firstLine,
    secondLine = this.secondLine,
    town = this.town,
    county = this.county,
    postcode = this.postcode,
    country = this.country,
    xCoordinate = this.xCoordinate,
    yCoordinate = this.yCoordinate,
    addressLastUpdated = this.addressLastUpdated,
  )

  private fun StandardAddressCheckRequest.toSummary(): StandardAddressCheckRequestSummary =
    StandardAddressCheckRequestSummary(
      requestId = this.id,
      caAdditionalInfo = this.caAdditionalInfo,
      ppAdditionalInfo = this.ppAdditionalInfo,
      preferencePriority = this.preferencePriority,
      dateRequested = this.dateRequested,
      status = this.status,
      address = this.address.toAddressSummary(),
    )

  private fun CasCheckRequest.toSummary(): CasCheckRequestSummary =
    CasCheckRequestSummary(
      caAdditionalInfo = this.caAdditionalInfo,
      ppAdditionalInfo = this.ppAdditionalInfo,
      preferencePriority = this.preferencePriority,
      dateRequested = this.dateRequested,
      status = this.status,
      allocatedAddress = this.allocatedAddress?.toAddressSummary(),
    )
}
