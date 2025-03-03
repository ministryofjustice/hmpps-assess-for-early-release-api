package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddCasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddResidentRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddStandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Resident
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.StandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
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
  private val assessmentRepository: AssessmentRepository,
) {
  fun getAddressesForPostcode(postcode: String): List<AddressSummary> = osPlacesApiClient.getAddressesForPostcode(postcode).map { it.toAddressSummary() }

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
//    agent: Agent,
  ): StandardAddressCheckRequestSummary {
    val uprn = addStandardAddressCheckRequest.addressUprn
    var address = addressRepository.findByUprn(uprn)
    if (address == null) {
      address = osPlacesApiClient.getAddressForUprn(uprn).toAddress()
      address = addressRepository.save(address)
    }

    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: error("Cannot find offender with prisonNumber $prisonNumber")
    val assessmentEntity = offender.currentAssessment()

    val standardAddressCheckRequest = standardAddressCheckRequestRepository.save(
      StandardAddressCheckRequest(
        caAdditionalInfo = addStandardAddressCheckRequest.caAdditionalInfo,
        ppAdditionalInfo = addStandardAddressCheckRequest.ppAdditionalInfo,
        preferencePriority = addStandardAddressCheckRequest.preferencePriority,
        address = address!!,
        assessment = offender.currentAssessment(),
      ),
    )

    assessmentEntity.recordEvent(
      changes = mapOf("standardAddressCheckRequest" to addStandardAddressCheckRequest.toSummary()),
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = SYSTEM_AGENT.toEntity(),
    )
    assessmentRepository.save(assessmentEntity)

    return standardAddressCheckRequest.toSummary()
  }

  @Transactional
  fun getStandardAddressCheckRequest(prisonNumber: String, requestId: Long): StandardAddressCheckRequestSummary = getStandardAddressCheckRequest(requestId, prisonNumber).toSummary()

  @Transactional
  fun addCasCheckRequest(
    prisonNumber: String,
    addCasCheckRequest: AddCasCheckRequest,
    agent: Agent,
  ): CasCheckRequestSummary {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: error("Cannot find offender with prisonNumber $prisonNumber")
    val assessmentEntity = offender.currentAssessment()

    val casCheckRequest = casCheckRequestRepository.save(
      CasCheckRequest(
        caAdditionalInfo = addCasCheckRequest.caAdditionalInfo,
        ppAdditionalInfo = addCasCheckRequest.ppAdditionalInfo,
        preferencePriority = addCasCheckRequest.preferencePriority,
        assessment = offender.currentAssessment(),
        allocatedAddress = null,
      ),
    )
    assessmentEntity.recordEvent(
      changes = mapOf("casCheckRequest" to addCasCheckRequest.toSummary()),
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = agent,
    )
    assessmentRepository.save(assessmentEntity)
    return casCheckRequest.toSummary()
  }

  @Transactional
  fun getCheckRequestsForAssessment(prisonNumber: String): List<CheckRequestSummary> {
    val assessment = assessmentService.getCurrentAssessment(prisonNumber)
    val checkRequests = curfewAddressCheckRequestRepository.findByAssessment(assessment)
    return checkRequests.map { it.toSummary() }
  }

  @Transactional
  fun deleteAddressCheckRequest(prisonNumber: String, requestId: Long, agent: Agent) {
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
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = agent,
    )
    assessmentRepository.save(assessmentEntity)

    curfewAddressCheckRequestRepository.delete(curfewAddressCheckRequest)
  }

  @Transactional
  fun addResidents(prisonNumber: String, requestId: Long, @Valid addResidentsRequest: List<AddResidentRequest>, agent: Agent): List<ResidentSummary> {
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
      changes = mapOf("existingResidents" to existingResidents, "newResidents" to addResidentsRequest.map { it.toSummary() }),
      eventType = AssessmentEventType.RESIDENT_UPDATED,
      agent = agent,
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
    agent: Agent,
  ) {
    val curfewAddressCheckRequest = getCurfewAddressCheckRequest(requestId, prisonNumber)
    curfewAddressCheckRequest.caAdditionalInfo = caseAdminInfoRequest.additionalInformation
    val assessmentEntity = curfewAddressCheckRequest.assessment
    assessmentEntity.recordEvent(
      changes = mapOf("caseAdminAdditionalInformation" to caseAdminInfoRequest.additionalInformation),
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = agent,
    )
    assessmentRepository.save(assessmentEntity)
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
  )

  private fun CasCheckRequest.toSummary(): CasCheckRequestSummary = CasCheckRequestSummary(
    requestId = this.id,
    caAdditionalInfo = this.caAdditionalInfo,
    ppAdditionalInfo = this.ppAdditionalInfo,
    preferencePriority = this.preferencePriority,
    dateRequested = this.dateRequested,
    status = this.status,
    allocatedAddress = this.allocatedAddress?.toAddressSummary(),
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

  private fun AddCasCheckRequest.toSummary(): AddCasCheckRequestSummary = AddCasCheckRequestSummary(
    caAdditionalInfo = this.caAdditionalInfo,
    ppAdditionalInfo = this.ppAdditionalInfo,
    preferencePriority = this.preferencePriority,
  )

  private fun CurfewAddressCheckRequest.toSummary(): CheckRequestSummary = when (this) {
    is StandardAddressCheckRequest -> this.toSummary()
    is CasCheckRequest -> this.toSummary()
    else -> error("Cannot transform request type of ${this::class.simpleName} to a check request summary")
  }
}
