package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Resident
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateCaseAdminAdditionInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiDPA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.getAddressFirstLine
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.toAddress

@Service
class AddressService(
  private val addressRepository: AddressRepository,
  private val assessmentService: AssessmentService,
  private val casCheckRequestRepository: CasCheckRequestRepository,
  private val curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository,
  private val offenderRepository: OffenderRepository,
  private val osPlacesApiClient: OsPlacesApiClient,
  private val standardAddressCheckRequestRepository: StandardAddressCheckRequestRepository,
  private val residentRepository: ResidentRepository,
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
  fun getStandardAddressCheckRequest(prisonNumber: String, requestId: Long): StandardAddressCheckRequestSummary =
    (getCurfewAddressCheckRequest(requestId, prisonNumber) as StandardAddressCheckRequest).toSummary()

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

  @Transactional
  fun getCheckRequestsForAssessment(prisonNumber: String): List<CheckRequestSummary> {
    val assessmentWithEligibilityProgress = assessmentService.getCurrentAssessment(prisonNumber)
    val assessment = assessmentWithEligibilityProgress.assessmentEntity
    val checkRequests = curfewAddressCheckRequestRepository.findByAssessment(assessment)
    return checkRequests.map { it.toSummary() }
  }

  @Transactional
  fun deleteAddressCheckRequest(prisonNumber: String, requestId: Long) {
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
        ?: throw EntityNotFoundException("Cannot find standard address check request with id: $requestId")

    if (curfewAddressCheckRequest.assessment.offender.prisonNumber != prisonNumber) {
      throw EntityNotFoundException(
        "Standard address check request id: $requestId is not linked to offender with prison number: $prisonNumber",
      )
    }

    curfewAddressCheckRequestRepository.delete(curfewAddressCheckRequest)
  }

  @Transactional
  fun addResident(prisonNumber: String, requestId: Long, addResidentRequest: AddResidentRequest): ResidentSummary {
    val standardAddressCheckRequest = getCurfewAddressCheckRequest(requestId, prisonNumber)

    var resident = Resident(
      forename = addResidentRequest.forename,
      surname = addResidentRequest.surname,
      phoneNumber = addResidentRequest.phoneNumber,
      relation = addResidentRequest.relation,
      dateOfBirth = addResidentRequest.dateOfBirth,
      age = addResidentRequest.age,
      isMainResident = addResidentRequest.isMainResident,
      standardAddressCheckRequest = standardAddressCheckRequest as StandardAddressCheckRequest,
    )
    resident = residentRepository.save(resident)
    return resident.toSummary()
  }

  @Transactional
  fun updateCaseAdminAdditionalInformation(
    prisonNumber: String,
    requestId: Long,
    caseAdminInfoRequest: UpdateCaseAdminAdditionInfoRequest,
  ) {
    val curfewAddressCheckRequest = getCurfewAddressCheckRequest(requestId, prisonNumber)
    curfewAddressCheckRequest.caAdditionalInfo = caseAdminInfoRequest.additionalInformation
    curfewAddressCheckRequestRepository.save(curfewAddressCheckRequest)
  }

  private fun getCurfewAddressCheckRequest(requestId: Long, prisonNumber: String): CurfewAddressCheckRequest {
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
        ?: throw EntityNotFoundException("Cannot find standard address check request with id: $requestId")

    if (curfewAddressCheckRequest.assessment.offender.prisonNumber != prisonNumber) {
      throw EntityNotFoundException(
        "Standard address check request id: $requestId is not linked to offender with prison number: $prisonNumber",
      )
    }
    return curfewAddressCheckRequest
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
      xcoordinate = this.xCoordinate,
      ycoordinate = this.yCoordinate,
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
    xcoordinate = this.xCoordinate,
    ycoordinate = this.yCoordinate,
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
      residents = this.residents.map { it.toSummary() },
    )

  private fun CasCheckRequest.toSummary(): CasCheckRequestSummary =
    CasCheckRequestSummary(
      requestId = this.id,
      caAdditionalInfo = this.caAdditionalInfo,
      ppAdditionalInfo = this.ppAdditionalInfo,
      preferencePriority = this.preferencePriority,
      dateRequested = this.dateRequested,
      status = this.status,
      allocatedAddress = this.allocatedAddress?.toAddressSummary(),
    )

  private fun Resident.toSummary(): ResidentSummary =
    ResidentSummary(
      residentId = this.id,
      forename = this.forename,
      surname = this.surname,
      phoneNumber = this.phoneNumber,
      relation = this.relation,
      dateOfBirth = this.dateOfBirth,
      age = this.age,
      isMainResident = this.isMainResident,
    )

  private fun CurfewAddressCheckRequest.toSummary(): CheckRequestSummary =
    when (this) {
      is StandardAddressCheckRequest -> this.toSummary()
      is CasCheckRequest -> this.toSummary()
      else -> error("Cannot transform request type of ${this::class.simpleName} to a check request summary")
    }
}
