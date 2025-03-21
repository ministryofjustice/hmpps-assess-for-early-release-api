package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddCasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddResidentRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddStandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CasAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.Resident
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.StandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddCasCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CasCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.UpdateCaseAdminAdditionInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasCheckRequestRepository
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
  private val casCheckRequestRepository: CasCheckRequestRepository,
  private val curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository,
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
        assessments = mutableListOf(currentAssessment),
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
  fun transferAddressChecks(
    fromAssessment: Assessment,
    toNewAssessment: Assessment) {

    val addressCheckRequest = curfewAddressCheckRequestRepository.findByAssessments(mutableListOf(fromAssessment))

    if (toNewAssessment.status != AssessmentStatus.NOT_STARTED) {
      throw ItemNotFoundException(
        "assessment: ${toNewAssessment.id} must not be started : ${toNewAssessment.status}",
      )
    }

    addressCheckRequest.forEach {
      it.assessments.add(toNewAssessment)
    }
  }

  @Transactional
  fun addCasAddressCheckRequest(
    prisonNumber: String,
    addCasCheckRequest: AddCasCheckRequest,
    agent: AgentDto,
  ): CasCheckRequestSummary {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)

    val casAddressCheckRequest = casCheckRequestRepository.save(
      CasAddressCheckRequest(
        caAdditionalInfo = addCasCheckRequest.caAdditionalInfo,
        ppAdditionalInfo = addCasCheckRequest.ppAdditionalInfo,
        preferencePriority = addCasCheckRequest.preferencePriority,
        assessments = mutableListOf(currentAssessment),
        allocatedAddress = null,
      ),
    )
    currentAssessment.recordEvent(
      changes = mapOf("casCheckRequest" to addCasCheckRequest.toSummary()),
      eventType = AssessmentEventType.ADDRESS_UPDATED,
      agent = agent.toEntity(),
    )
    assessmentRepository.save(currentAssessment)
    return casAddressCheckRequest.toSummary()
  }

  @Transactional
  fun getCheckRequestsForAssessment(prisonNumber: String): List<CheckRequestSummary> {
    val currentAssessment = assessmentService.getCurrentAssessment(prisonNumber)
    val checkRequests = curfewAddressCheckRequestRepository.findByAssessments(mutableListOf(currentAssessment))
    return checkRequests.map { it.toSummary() }
  }

  @Transactional
  fun deleteAddressCheckRequest(prisonNumber: String, requestId: Long, agent: AgentDto) {
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
        ?: throw ItemNotFoundException("Cannot find standard address check request with id: $requestId")

    if (isAnAssessmentAssociatedWithTheAddressCheck(curfewAddressCheckRequest, prisonNumber)) {
      throw ItemNotFoundException(
        "Standard address check request id: $requestId is not linked to offender with prison number: $prisonNumber",
      )
    }

    val changes = mapOf("deleteAddressCheckRequestId" to requestId)
    recordEventsOnAssessment(curfewAddressCheckRequest, changes, AssessmentEventType.ADDRESS_UPDATED, agent)

    assessmentRepository.saveAllAndFlush(curfewAddressCheckRequest.assessments)
    curfewAddressCheckRequestRepository.delete(curfewAddressCheckRequest)
  }

  private fun isAnAssessmentAssociatedWithTheAddressCheck(
    curfewAddressCheckRequest: CurfewAddressCheckRequest,
    prisonNumber: String,
  ) = curfewAddressCheckRequest.assessments.any { it.offender.prisonNumber == prisonNumber }

  @Transactional
  fun addResidents(
    prisonNumber: String,
    requestId: Long,
    @Valid addResidentsRequest: List<AddResidentRequest>,
    agent: AgentDto,
  ): List<ResidentSummary> {
    val addressCheckRequest = getStandardAddressCheckRequest(requestId, prisonNumber)

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

    val changes = mapOf(
      "existingResidents" to existingResidents,
      "newResidents" to addResidentsRequest.map { it.toSummary() },
    )
    recordEventsOnAssessment(addressCheckRequest, changes, AssessmentEventType.RESIDENT_UPDATED, agent)

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
    val changes = mapOf(
      "caseAdminAdditionalInformation" to caseAdminInfoRequest.additionalInformation,
    )
    recordEventsOnAssessment(curfewAddressCheckRequest, changes, AssessmentEventType.ADDRESS_UPDATED, agent)
    curfewAddressCheckRequestRepository.save(curfewAddressCheckRequest)
  }

  fun getCurfewAddressCheckRequest(requestId: Long, prisonNumber: String): CurfewAddressCheckRequest {
    val curfewAddressCheckRequest =
      curfewAddressCheckRequestRepository.findByIdOrNull(requestId)
        ?: throw ItemNotFoundException("Cannot find curfew address check request with id: $requestId")

    if (isAnAssessmentAssociatedWithTheAddressCheck(curfewAddressCheckRequest, prisonNumber)) {
      throw ItemNotFoundException(
        "Curfew address check request id: $requestId is not linked to offender with prison number: $prisonNumber",
      )
    }
    return curfewAddressCheckRequest
  }

  private fun recordEventsOnAssessment(
    curfewAddressCheckRequest: CurfewAddressCheckRequest,
    changes: Map<String, Any>,
    eventType: AssessmentEventType,
    agent: AgentDto,
  ) {
    curfewAddressCheckRequest.assessments.forEach { assessment ->
      assessment.recordEvent(
        changes = changes,
        eventType = eventType,
        agent = agent.toEntity(),
      )
    }
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

  private fun CasAddressCheckRequest.toSummary(): CasCheckRequestSummary = CasCheckRequestSummary(
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
    is CasAddressCheckRequest -> this.toSummary()
    else -> error("Cannot transform request type of ${this::class.simpleName} to a check request summary")
  }
}
