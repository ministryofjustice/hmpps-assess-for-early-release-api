package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasAccommodationAssessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.Address
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.AddPrisonerEligibilityInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.Cas2ReferralInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentAddressRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentOutcomeRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationAssessmentTypeRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationStatusInfoResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasOutcomeType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.FlagCasAccommodationAssessmentForReferralRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CasAccommodationAssessmentRepository
import java.time.LocalDate

@Service
class CasAccommodationAssessmentService(
  private val casAccommodationAssessmentRepository: CasAccommodationAssessmentRepository,
  private val assessmentService: AssessmentService,
  private val addressService: AddressService,
  private val addressRepository: AddressRepository,

) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun prisonerRequestsCasAssessment(prisonNumber: String, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val assessment = assessmentService.getCurrentAssessment(prisonNumber)

    val casAssessment = CasAccommodationAssessment(
      assessment = assessment,
      status = CasStatus.PROPOSED,
    )
    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    assessment.casAccommodationAssessments.add(savedCasAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  @Transactional
  fun addPrisonerEligibilityInfo(accommodationAssessmentId: Long, addPrisonerEligibilityInfoRequest: AddPrisonerEligibilityInfoRequest, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val casAssessment = getCasAssessment(accommodationAssessmentId)
    when (addPrisonerEligibilityInfoRequest.eligibleForCas) {
      true -> casAssessment.setStatus(CasStatus.PERSON_ELIGIBLE)
      false -> {
        casAssessment.setStatus(CasStatus.PERSON_INELIGIBLE)
        casAssessment.ineligibilityReason = addPrisonerEligibilityInfoRequest.ineligibilityReason
      }
    }
    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  @Transactional
  fun setCasType(accommodationAssessmentId: Long, casAccommodationAssessmentTypeRequest: CasAccommodationAssessmentTypeRequest, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val casAssessment = getCasAssessment(accommodationAssessmentId)
    casAssessment.type = casAccommodationAssessmentTypeRequest.casType
    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  @Transactional
  fun addCas2ReferralInfo(accommodationAssessmentId: Long, cas2ReferralInfoRequest: Cas2ReferralInfoRequest, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val casAssessment = getCasAssessment(accommodationAssessmentId)
    check(casAssessment.type == CasType.CAS_2) {
      "Cas assessment is not a Cas2 assessment type:${casAssessment.type}" // TODAY add to others
    }
    casAssessment.areasToAvoidInfo = cas2ReferralInfoRequest.areasToAvoidInfo
    casAssessment.supportingInfoForReferral = cas2ReferralInfoRequest.supportingInfoForReferral
    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  @Transactional
  fun flagForReferral(accommodationAssessmentId: Long, flagCasAccommodationAssessmentForReferralRequest: FlagCasAccommodationAssessmentForReferralRequest, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val casAssessment = getCasAssessment(accommodationAssessmentId)
    if (flagCasAccommodationAssessmentForReferralRequest.isReferred) {
      casAssessment.setStatus(CasStatus.REFERRAL_REQUESTED)
      casAssessment.referred = true
    } else {
      casAssessment.referred = false
    }
    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  @Transactional
  fun addOutcome(accommodationAssessmentId: Long, casAccommodationAssessmentOutcomeRequest: CasAccommodationAssessmentOutcomeRequest, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val casAssessment = getCasAssessment(accommodationAssessmentId)
    when (casAccommodationAssessmentOutcomeRequest.outcomeType) {
      CasOutcomeType.REFERRAL_ACCEPTED -> casAssessment.setStatus(CasStatus.REFERRAL_ACCEPTED)
      CasOutcomeType.REFERRAL_REFUSED -> casAssessment.setStatus(CasStatus.REFERRAL_REFUSED)
      CasOutcomeType.REFERRAL_WITHDRAWN -> casAssessment.setStatus(CasStatus.REFERRAL_WITHDRAWN)
    }
    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  // Change to add manual address
  @Transactional
  fun addAddress(accommodationAssessmentId: Long, casAccommodationAssessmentAddressRequest: CasAccommodationAssessmentAddressRequest, agent: AgentDto): CasAccommodationStatusInfoResponse {
    val casAssessment = getCasAssessment(accommodationAssessmentId)
    casAssessment.setStatus(CasStatus.ADDRESS_PROVIDED)

    val cleanedPostCode = casAccommodationAssessmentAddressRequest.postCode.uppercase().replace(" ", "")
    val (county, country) = addressService.getCountyAndCountryForPostCode(cleanedPostCode)
    with(casAccommodationAssessmentAddressRequest) {
      casAssessment.address = addressRepository.saveAndFlush(
        Address(
          firstLine = line1,
          secondLine = line2,
          town = townOrCity,
          county = county,
          postcode = cleanedPostCode,
          country = country,
          addressLastUpdated = LocalDate.now(),
        ),
      )
    }

    val savedCasAssessment = casAccommodationAssessmentRepository.saveAndFlush(casAssessment)
    return CasAccommodationStatusInfoResponse(savedCasAssessment.id, savedCasAssessment.getStatus())
  }

  private fun getCasAssessment(accommodationAssessmentId: Long) = casAccommodationAssessmentRepository.findById(accommodationAssessmentId).orElseThrow {
    throw ItemNotFoundException("Cannot find cas accommodationAssessment with id: $accommodationAssessmentId")
  }
}
