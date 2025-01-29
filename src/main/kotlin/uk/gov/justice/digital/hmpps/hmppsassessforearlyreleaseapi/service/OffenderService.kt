package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.Companion.getStatusesForRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.DeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val PRISONER_CREATED_EVENT_NAME = "assess-for-early-release.prisoner.created"
const val PRISONER_UPDATED_EVENT_NAME = "assess-for-early-release.prisoner.updated"

@Service
class OffenderService(
  private val assessmentRepository: AssessmentRepository,
  private val offenderRepository: OffenderRepository,
  private val prisonerSearchService: PrisonerSearchService,
  private val probationService: ProbationService,
  private val staffRepository: StaffRepository,
  private val telemetryClient: TelemetryClient,
) {
  @Transactional
  fun getCaseAdminCaseload(prisonCode: String): List<OffenderSummary> {
    val offenders = offenderRepository.findByPrisonIdAndStatusIn(prisonCode, getStatusesForRole(UserRole.PRISON_CA))
    return offenders.map {
      OffenderSummary(it.prisonNumber, it.bookingId, it.forename, it.surname, it.hdced)
    }
  }

  @Transactional
  fun getComCaseload(staffCode: String): List<OffenderSummary> {
    val assessments = assessmentRepository.findByResponsibleComStaffCodeAndStatusIn(staffCode, getStatusesForRole(UserRole.PROBATION_COM))
    return assessments.map { assessment ->
      val offender = assessment.offender
      OffenderSummary(
        offender.prisonNumber,
        offender.bookingId,
        offender.forename,
        offender.surname,
        offender.hdced,
        assessment.responsibleCom?.fullName,
      )
    }
  }

  @Transactional
  fun getDecisionMakerCaseload(prisonCode: String): List<OffenderSummary> {
    val assessments = assessmentRepository.findAllByOffenderPrisonIdAndStatusIn(prisonCode, getStatusesForRole(UserRole.PRISON_DM))
    return assessments.map { assessment ->
      val offender = assessment.offender
      OffenderSummary(
        offender.prisonNumber,
        offender.bookingId,
        offender.forename,
        offender.surname,
        offender.hdced,
        assessment.responsibleCom?.fullName,
      )
    }
  }

  fun createOrUpdateOffender(nomisId: String) {
    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(listOf(nomisId))
    if (prisoners.isEmpty()) {
      val msg = "Could not find prisoner with prisonNumber $nomisId in prisoner search"
      log.warn(msg)
      error(msg)
    }

    val prisoner = prisoners.first()
    if (prisoner.homeDetentionCurfewEligibilityDate != null) {
      val offender = offenderRepository.findByPrisonNumber(nomisId)
      if (offender != null) {
        updateOffender(offender, prisoner)
      } else {
        createOffender(prisoner)
      }
    }
  }

  private fun createOffender(prisoner: PrisonerSearchPrisoner) {
    val offender = Offender(
      bookingId = prisoner.bookingId!!.toLong(),
      prisonNumber = prisoner.prisonerNumber,
      prisonId = prisoner.prisonId!!,
      forename = prisoner.firstName,
      surname = prisoner.lastName,
      dateOfBirth = prisoner.dateOfBirth,
      hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
      crd = prisoner.conditionalReleaseDate,
    )

    val communityOffenderManager = probationService.getCurrentResponsibleOfficer(prisoner.prisonerNumber)?.let {
      staffRepository.findByStaffCode(it.code) ?: createCommunityOffenderManager(it)
    }

    val assessment = Assessment(
      offender = offender,
      policyVersion = PolicyService.CURRENT_POLICY_VERSION.code,
      responsibleCom = communityOffenderManager,
    )

    offender.assessments.add(assessment)
    offenderRepository.save(offender)

    telemetryClient.trackEvent(
      PRISONER_CREATED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to prisoner.prisonerNumber,
        "PRISONER_HDCED" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
      ),
      null,
    )
  }

  private fun updateOffender(offender: Offender, prisoner: PrisonerSearchPrisoner) {
    if (hasOffenderBeenUpdated(offender, prisoner)) {
      val updatedOffender = offender.copy(
        forename = prisoner.firstName,
        surname = prisoner.lastName,
        dateOfBirth = prisoner.dateOfBirth,
        hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
        crd = prisoner.conditionalReleaseDate,
        lastUpdatedTimestamp = LocalDateTime.now(),
      )
      offenderRepository.save(updatedOffender)
      telemetryClient.trackEvent(
        PRISONER_UPDATED_EVENT_NAME,
        mapOf(
          "NOMS-ID" to prisoner.prisonerNumber,
          "PRISONER-FIRST_NAME" to prisoner.firstName,
          "PRISONER-LAST_NAME" to prisoner.lastName,
          "PRISONER_DOB" to prisoner.dateOfBirth.format(DateTimeFormatter.ISO_DATE),
          "PRISONER_HDCED" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
        ),
        null,
      )
    }
  }

  private fun hasOffenderBeenUpdated(offender: Offender, prisoner: PrisonerSearchPrisoner) = offender.hdced != prisoner.homeDetentionCurfewEligibilityDate ||
    offender.crd != prisoner.conditionalReleaseDate ||
    offender.forename != prisoner.firstName ||
    offender.surname != prisoner.lastName ||
    offender.dateOfBirth != prisoner.dateOfBirth

  private fun createCommunityOffenderManager(offenderManager: DeliusOffenderManager): CommunityOffenderManager = staffRepository.save(
    CommunityOffenderManager(
      staffCode = offenderManager.code,
      username = offenderManager.username,
      email = offenderManager.email,
      forename = offenderManager.name.forename,
      surname = offenderManager.name.surname,
      team = offenderManager.team.code,
    ),
  )

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
