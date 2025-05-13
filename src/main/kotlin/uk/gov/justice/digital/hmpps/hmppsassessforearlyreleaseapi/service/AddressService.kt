package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressDeletionEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddResidentRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddStandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.Resident
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.StandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressDeleteReasonDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.UpdateCaseAdminAdditionInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StandardAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.OsPlacesApiDPA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.getAddressFirstLine
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os.toAddress

@Service
class AddressService(
  private val addressRepository: AddressRepository,
  private val assessmentService: AssessmentService,
  private val curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository,
  private val osPlacesApiClient: OsPlacesApiClient,
  private val standardAddressCheckRequestRepository: StandardAddressCheckRequestRepository,
  private val residentRepository: ResidentRepository,
  private val assessmentRepository: AssessmentRepository,
) {
  fun searchForAddresses(searchQuery: String): List<AddressSummary> = osPlacesApiClient.searchForAddresses(searchQuery).map { it.toAddressSummary() }

  fun searchForAddressesWithPostcode(postcode: String): List<AddressSummary> = osPlacesApiClient.getAddressesForPostcode(postcode).map { it.toAddressSummary() }

  fun isValidPostcode(postcode: String): Boolean = postcode.matches(Regex("[A-Z]{1,2}[0-9R][0-9A-Z]?[0-9][ABD-HJLNP-UW-Z]{2}\$"))

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
    agent: AgentDto,
  ): StandardAddressCheckRequestSummary {
    val uprn = addStandardAddressCheckRequest.addressUprn
    var address = addressRepository.findByUprn(uprn)
    if (address == null) {
      address = osPlacesApiClient.getAddressForUprn(uprn).toAddress()
      address = addressRepository.save(address)
    }

    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)

    val standardAddressCheckRequest = standardAddressCheckRequestRepository.save(
      StandardAddressCheckRequest(
        caAdditionalInfo = addStandardAddressCheckRequest.caAdditionalInfo,
        ppAdditionalInfo = addStandardAddressCheckRequest.ppAdditionalInfo,
        preferencePriority = addStandardAddressCheckRequest.preferencePriority,
        address = address!!,
        assessment = currentAssessment,
      ),
    )

    currentAssessment.recordEvent(
      changes = mapOf("standardAddressCheckRequest" to addStandardAddressCheckRequest.toSummary()),
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = agent.toEntity(),
    )
    assessmentRepository.save(currentAssessment)

    return standardAddressCheckRequest.toSummary()
  }

  @Transactional
  fun getStandardAddressCheckRequest(prisonNumber: String, requestId: Long): StandardAddressCheckRequestSummary = getStandardAddressCheckRequest(requestId, prisonNumber).toSummary()

  @Transactional
  fun getCheckRequestsForAssessment(prisonNumber: String): List<CheckRequestSummary> {
    val assessment = assessmentService.getCurrentAssessment(prisonNumber)
    val checkRequests = curfewAddressCheckRequestRepository.findByAssessment(assessment)
    return checkRequests.map { it.toSummary() }
  }

  @Transactional
  fun deleteAddressCheckRequest(prisonNumber: String, requestId: Long, agent: AgentDto) {
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
        ?: throw ItemNotFoundException("Cannot find standard address check request with id: $requestId")

    if (curfewAddressCheckRequest.assessment.offender.prisonNumber != prisonNumber) {
      throw ItemNotFoundException(
        "Standard address check request id: $requestId is not linked to offender with prison number: $prisonNumber",
      )
    }
    val assessmentEntity = curfewAddressCheckRequest.assessment
    assessmentEntity.recordEvent(
      changes = mapOf("deleteAddressCheckRequestId" to requestId),
      eventType = AssessmentEventType.ADDRESS_DELETED,
      agent = agent.toEntity(),
    )
    assessmentRepository.save(assessmentEntity)

    curfewAddressCheckRequestRepository.delete(curfewAddressCheckRequest)
  }

  @Transactional
  fun addResidents(
    prisonNumber: String,
    requestId: Long,
    @Valid addResidentsRequest: List<AddResidentRequest>,
    agent: AgentDto,
  ): List<ResidentSummary> {
    val addressCheckRequest = getStandardAddressCheckRequest(requestId, prisonNumber)
    val assessmentEntity = addressCheckRequest.assessment

    // Retrieve existing residents linked to the requestId
    val existingResidents = residentRepository.findByStandardAddressCheckRequestId(requestId)

    // Identify residents to delete
    val addResidentIds = addResidentsRequest.mapNotNull { it.residentId }
    val (residentsToDelete, recordsToUpdate) = existingResidents.partition { it.id !in addResidentIds }

    // Delete residents not present in addResidentsRequest
    if (residentsToDelete.isNotEmpty()) {
      residentRepository.deleteAll(residentsToDelete)
    }

    val residentsToSave = addResidentsRequest.map { addResidentRequest ->

      val existingResident = recordsToUpdate.find { it.id == addResidentRequest.residentId }
      existingResident?.apply {
        forename = addResidentRequest.forename
        surname = addResidentRequest.surname
        phoneNumber = addResidentRequest.phoneNumber
        relation = addResidentRequest.relation
        dateOfBirth = addResidentRequest.dateOfBirth
        age = addResidentRequest.age
        isMainResident = addResidentRequest.isMainResident
        isOffender = addResidentRequest.isOffender
      }
        ?: Resident(
          forename = addResidentRequest.forename,
          surname = addResidentRequest.surname,
          phoneNumber = addResidentRequest.phoneNumber,
          relation = addResidentRequest.relation,
          dateOfBirth = addResidentRequest.dateOfBirth,
          age = addResidentRequest.age,
          isMainResident = addResidentRequest.isMainResident,
          isOffender = addResidentRequest.isOffender,
          standardAddressCheckRequest = addressCheckRequest,
        )
    }

    assessmentEntity.recordEvent(
      changes = mapOf(
        "existingResidents" to existingResidents,
        "newResidents" to addResidentsRequest.map { it.toSummary() },
      ),
      eventType = AssessmentEventType.RESIDENT_UPDATED,
      agent = agent.toEntity(),
    )
    assessmentRepository.save(assessmentEntity)

    val savedResidents = residentRepository.saveAllAndFlush(residentsToSave)
    return savedResidents.map { it!!.toSummary() }
  }

  @Transactional
  fun updateCaseAdminAdditionalInformation(
    prisonNumber: String,
    requestId: Long,
    caseAdminInfoRequest: UpdateCaseAdminAdditionInfoRequest,
    agent: AgentDto,
  ) {
    val curfewAddressCheckRequest = getCurfewAddressCheckRequest(requestId, prisonNumber)
    curfewAddressCheckRequest.caAdditionalInfo = caseAdminInfoRequest.additionalInformation
    val assessmentEntity = curfewAddressCheckRequest.assessment
    assessmentEntity.recordEvent(
      changes = mapOf("caseAdminAdditionalInformation" to caseAdminInfoRequest.additionalInformation),
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = agent.toEntity(),
    )
    assessmentRepository.save(assessmentEntity)
    curfewAddressCheckRequestRepository.save(curfewAddressCheckRequest)
  }

  @Transactional
  fun withdrawAddress(
    prisonNumber: String,
    requestId: Long,
    addressDeleteReason: AddressDeleteReasonDto,
    agent: AgentDto,
  ) {
    val curfewAddressCheckRequest = getCurfewAddressCheckRequest(requestId, prisonNumber)
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    currentAssessment.recordEvent(
      changes = mapOf(
        "addressDeleteReasonType" to addressDeleteReason.addressDeleteReasonType.toString(),
        "otherDeleteReasonDetails" to addressDeleteReason.addressDeleteOtherReason.toString(),
      ),
      eventType = AssessmentEventType.ADDRESS_DELETED,
      agent = agent.toEntity(),
    )
    assessmentRepository.save(currentAssessment)

    val addressDeletionEvent = AddressDeletionEvent(
      reasonType = addressDeleteReason.addressDeleteReasonType,
      otherReason = addressDeleteReason.addressDeleteOtherReason,
      assessmentEvent = currentAssessment.lastUpdateByUserEvent,
    )
    curfewAddressCheckRequest.addressDeletionEvent = addressDeletionEvent
    curfewAddressCheckRequestRepository.save(curfewAddressCheckRequest)
  }

  fun getCurfewAddressCheckRequest(requestId: Long, prisonNumber: String): CurfewAddressCheckRequest {
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
        ?: throw ItemNotFoundException("Cannot find curfew address check request with id: $requestId")

    if (curfewAddressCheckRequest.assessment.offender.prisonNumber != prisonNumber) {
      throw ItemNotFoundException(
        "Curfew address check request id: $requestId is not linked to offender with prison number: $prisonNumber",
      )
    }
    return curfewAddressCheckRequest
  }

  @Cacheable("countyAndCountryForPostCode")
  fun getCountyAndCountryForPostCode(postCode: String): Pair<String?, String?> {
    if (isValidPostcode(postCode)) {
      val results = searchForAddressesWithPostcode(postCode)
      if (results.isNotEmpty()) {
        with(results.first()) {
          return Pair(county, country)
        }
      }
    }
    return Pair(null, null)
  }

  private fun getStandardAddressCheckRequest(requestId: Long, prisonNumber: String): StandardAddressCheckRequest {
    val curfewAddressCheckRequest = getCurfewAddressCheckRequest(requestId, prisonNumber)
    if (curfewAddressCheckRequest !is StandardAddressCheckRequest) {
      throw ItemNotFoundException("Cannot find a standard address check request with id: $requestId")
    }
    return curfewAddressCheckRequest
  }

  private fun OsPlacesApiDPA.toAddressSummary(): AddressSummary = AddressSummary(
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

  private fun StandardAddressCheckRequest.toSummary(): StandardAddressCheckRequestSummary = StandardAddressCheckRequestSummary(
    requestId = this.id,
    caAdditionalInfo = this.caAdditionalInfo,
    ppAdditionalInfo = this.ppAdditionalInfo,
    preferencePriority = this.preferencePriority,
    dateRequested = this.dateRequested,
    status = this.status,
    address = this.address.toAddressSummary(),
    residents = this.residents.map { it.toSummary() },
    deleteReason = AddressDeleteReasonDto(
      this.addressDeletionEvent?.reasonType,
      this.addressDeletionEvent?.otherReason,
    ),
  )

  private fun Resident.toSummary(): ResidentSummary = ResidentSummary(
    residentId = this.id,
    forename = this.forename,
    surname = this.surname,
    phoneNumber = this.phoneNumber,
    relation = this.relation,
    dateOfBirth = this.dateOfBirth,
    age = this.age,
    isMainResident = this.isMainResident,
    isOffender = this.isOffender,
  )

  private fun AddResidentRequest.toSummary(): AddResidentRequestSummary = AddResidentRequestSummary(
    residentId = this.residentId,
    forename = this.forename,
    surname = this.surname,
    phoneNumber = this.phoneNumber,
    relation = this.relation,
    dateOfBirth = this.dateOfBirth,
    age = this.age,
    isMainResident = this.isMainResident,
    isOffender = this.isOffender,
  )

  private fun AddStandardAddressCheckRequest.toSummary(): AddStandardAddressCheckRequestSummary = AddStandardAddressCheckRequestSummary(
    caAdditionalInfo = this.caAdditionalInfo,
    ppAdditionalInfo = this.ppAdditionalInfo,
    preferencePriority = this.preferencePriority,
    addressUprn = this.addressUprn,
  )

  private fun CurfewAddressCheckRequest.toSummary(): CheckRequestSummary = when (this) {
    is StandardAddressCheckRequest -> this.toSummary()
    else -> error("Cannot transform request type of ${this::class.simpleName} to a check request summary")
  }
}
