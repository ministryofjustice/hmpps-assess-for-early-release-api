package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.PostponementReasonEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.staff.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.CompleteAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.OptBackIn
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.OptOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.ResidentialCheckAnswerProvided
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent.SubmitForAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.PostponeCaseRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toModel
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getSuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.AssessmentToAssessmentOverviewSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Policy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.DeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class AssessmentService(
  private val assessmentRepository: AssessmentRepository,
  private val offenderToAssessmentSummaryMapper: OffenderToAssessmentSummaryMapper,
  private val assessmentToAssessmentOverviewSummaryMapper: AssessmentToAssessmentOverviewSummaryMapper,
  private val prisonService: PrisonService,
  private val policyService: PolicyService,
  private val telemetryClient: TelemetryClient,
  private val staffRepository: StaffRepository,
  @org.springframework.context.annotation.Lazy
  private val probationService: ProbationService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getCurrentAssessment(prisonNumber: String): Assessment {
    val assessments = assessmentRepository.findByOffenderPrisonNumberAndDeletedTimestampIsNullOrderByCreatedTimestamp(prisonNumber)
    if (assessments.isEmpty()) {
      throw ItemNotFoundException("Cannot find current assessment with prisonNumber $prisonNumber")
    }
    return assessments.last()
  }

  @Transactional
  fun getCurrentAssessmentSummary(prisonNumber: String): AssessmentSummary {
    val currentAssessment = getCurrentAssessment(prisonNumber)
    return offenderToAssessmentSummaryMapper.map(currentAssessment)
  }

  @Transactional
  fun getAssessmentOverviewSummary(prisonNumber: String): AssessmentOverviewSummary {
    val currentAssessment = getCurrentAssessment(prisonNumber)
    val offender = currentAssessment.offender

    val prisonName = prisonService.getPrisonNameForId(offender.prisonId)
    val prisonerSearchResults = getPrisonerDetails(offender).first()
    val assessmentWithEligibilityProgress = getCurrentAssessmentWithEligibilityProgress(currentAssessment)

    return assessmentToAssessmentOverviewSummaryMapper.map(assessmentWithEligibilityProgress, prisonName, prisonerSearchResults)
  }

  @Transactional
  fun transitionAssessment(assessmentEntity: Assessment, event: AssessmentLifecycleEvent, agentDto: AgentDto?) {
    assessmentEntity.performTransition(event, agentDto.toEntity())
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun postponeCase(prisonNumber: String, postponeCaseRequest: PostponeCaseRequest) {
    val assessmentEntity = getCurrentAssessment(prisonNumber)

    assessmentEntity.performTransition(AssessmentLifecycleEvent.Postpone(postponeCaseRequest.reasonTypes), postponeCaseRequest.agent.toEntity())

    val reasonTypes = postponeCaseRequest.reasonTypes.map { reasonType ->
      PostponementReasonEntity(reasonType = reasonType, assessment = assessmentEntity)
    }.toList()

    assessmentEntity.postponementReasons.addAll(reasonTypes)
    assessmentEntity.postponementDate = LocalDate.now()

    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun optOut(prisonNumber: String, optOutRequest: OptOutRequest) {
    val assessmentEntity = getCurrentAssessment(prisonNumber)
    assessmentEntity.performTransition(OptOut(optOutRequest.reasonType, optOutRequest.otherDescription), optOutRequest.agent.toEntity())
    assessmentEntity.optOutReasonType = optOutRequest.reasonType
    assessmentEntity.optOutReasonOther = optOutRequest.otherDescription
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun optBackIn(prisonNumber: String, agentDto: AgentDto) {
    val assessmentEntity = getCurrentAssessment(prisonNumber)
    assessmentEntity.performTransition(OptBackIn, agentDto.toEntity())
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun submitAssessmentForAddressChecks(prisonNumber: String, agentDto: AgentDto) {
    val assessmentEntity = getCurrentAssessment(prisonNumber)
    assessmentEntity.performTransition(SubmitForAddressChecks, agentDto.toEntity())
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun submitForPreDecisionChecks(prisonNumber: String, agentDto: AgentDto) {
    val assessmentEntity = getCurrentAssessment(prisonNumber)
    assessmentEntity.performTransition(CompleteAddressChecks, agentDto.toEntity())
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun updateAddressChecksStatus(prisonNumber: String, status: ResidentialChecksStatus, request: SaveResidentialChecksTaskAnswersRequest) {
    val event = ResidentialCheckAnswerProvided(status, request.taskCode, request.answers)

    val assessmentEntity = getCurrentAssessment(prisonNumber)
    assessmentEntity.performTransition(event, request.agent.toEntity())

    if (status == SUITABLE && !assessmentEntity.addressChecksComplete) {
      assessmentEntity.addressChecksComplete = true
    } else if (status != SUITABLE && assessmentEntity.addressChecksComplete) {
      assessmentEntity.addressChecksComplete = false
    }
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun updateTeamForResponsibleCom(staffCode: String, team: String) {
    var comsAssessments = assessmentRepository.findByResponsibleComStaffCodeAndStatusInAndDeletedTimestampIsNull(staffCode, AssessmentStatus.inFlightStatuses())
    comsAssessments = comsAssessments.map {
      it.copy(team = team)
    }
    assessmentRepository.saveAll(comsAssessments)
  }

  data class AssessmentWithEligibilityProgress(
    val assessmentEntity: Assessment,
    val policy: Policy,
  ) {
    val offender: Offender = assessmentEntity.offender

    fun getEligibilityProgress(): List<EligibilityCriterionProgress> {
      val codeToChecks = this.assessmentEntity.eligibilityCheckResults
        .filter { it.criterionType == ELIGIBILITY }
        .associateBy { it.criterionCode }

      return policy.eligibilityCriteria.map { it.toEligibilityCriterionProgress(codeToChecks[it.code]) }
    }

    private fun Criterion.toEligibilityCriterionProgress(eligibilityCheckResult: EligibilityCheckResult?): EligibilityCriterionProgress = EligibilityCriterionProgress(
      code = code,
      taskName = name,
      status = eligibilityCheckResult.getEligibilityStatus(),
      questions = questions.map {
        Question(
          text = it.text,
          hint = it.hint,
          name = it.name,
          answer = eligibilityCheckResult.getAnswer(it.name),
        )
      },
      agent = eligibilityCheckResult?.agent?.toModel(),
      lastUpdated = eligibilityCheckResult?.lastUpdatedTimestamp?.toLocalDate(),
    )

    fun getSuitabilityProgress(): List<SuitabilityCriterionProgress> {
      val codeToChecks = this.assessmentEntity.eligibilityCheckResults
        .filter { it.criterionType == SUITABILITY }
        .associateBy { it.criterionCode }

      return policy.suitabilityCriteria.map { it.toSuitabilityCriterionProgress(codeToChecks[it.code]) }
    }

    private fun Criterion.toSuitabilityCriterionProgress(eligibilityCheckResult: EligibilityCheckResult?) = SuitabilityCriterionProgress(
      code = code,
      taskName = name,
      status = eligibilityCheckResult.getSuitabilityStatus(),
      questions = questions.map {
        Question(
          text = it.text,
          hint = it.hint,
          name = it.name,
          answer = eligibilityCheckResult.getAnswer(it.name),
        )
      },
      agent = eligibilityCheckResult?.agent?.toModel(),
      lastUpdated = eligibilityCheckResult?.lastUpdatedTimestamp?.toLocalDate(),
    )
  }

  @Transactional
  fun createAssessment(offender: Offender, prisonerNumber: String, bookingId: Long): Assessment {
    log.debug("Creating assessment for prisonerNumber: {}", prisonerNumber)

    val deliusOffenderManager = offender.crn?.let {
      probationService.getCurrentResponsibleOfficer(it)
    }

    val communityOffenderManager = offender.crn?.let {
      deliusOffenderManager?.let {
        staffRepository.findByStaffCode(it.code) ?: createCommunityOffenderManager(it)
      }
    }

    val assessment = Assessment(
      bookingId = bookingId,
      offender = offender,
      policyVersion = PolicyService.CURRENT_POLICY_VERSION.code,
      responsibleCom = communityOffenderManager,
      team = deliusOffenderManager?.team?.code,
    )

    val changes = mapOf(
      "prisonNumber" to prisonerNumber,
      "homeDetentionCurfewEligibilityDate" to offender.hdced.format(DateTimeFormatter.ISO_DATE),
    )

    assessment.recordEvent(
      eventType = AssessmentEventType.PRISONER_CREATED,
      changes,
      agent = SYSTEM_AGENT,
    )

    return assessmentRepository.save(assessment)
  }

  private fun createCommunityOffenderManager(offenderManager: DeliusOffenderManager): CommunityOffenderManager = staffRepository.save(
    CommunityOffenderManager(
      staffCode = offenderManager.code,
      username = offenderManager.username,
      email = offenderManager.email,
      forename = offenderManager.name.forename,
      surname = offenderManager.name.surname,
    ),
  )

  private fun getPrisonerDetails(offender: Offender): List<PrisonerSearchPrisoner> {
    val prisonerSearchResults = prisonService.searchPrisonersByNomisIds(listOf(offender.prisonNumber))
    if (prisonerSearchResults.isEmpty()) {
      throw ItemNotFoundException("Could not find prisoner details for ${offender.prisonNumber}")
    }
    return prisonerSearchResults
  }

  private fun getCurrentAssessmentWithEligibilityProgress(currentAssessment: Assessment): AssessmentWithEligibilityProgress {
    val policy = policyService.getVersionFromPolicy(currentAssessment.policyVersion)
    return AssessmentWithEligibilityProgress(
      assessmentEntity = currentAssessment,
      policy = policy,
    )
  }
}
