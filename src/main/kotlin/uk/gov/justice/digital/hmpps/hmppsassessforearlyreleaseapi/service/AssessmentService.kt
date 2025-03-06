package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.CompleteAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.OptBackIn
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.OptOut
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentLifecycleEvent.SubmitForAddressChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.EligibilityCheckResult
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.PostponementReasonEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.*
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getEligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StatusHelpers.getSuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Policy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import java.time.LocalDate

@Service
class AssessmentService(
  private val policyService: PolicyService,
  private val offenderRepository: OffenderRepository,
  private val assessmentRepository: AssessmentRepository,
  private val offenderToAssessmentSummaryMapper: OffenderToAssessmentSummaryMapper,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private fun getOffender(prisonNumber: String) = offenderRepository.findByPrisonNumber(prisonNumber)
    ?: throw ItemNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

  @Transactional
  fun getCurrentAssessment(prisonNumber: String) = getOffender(prisonNumber).currentAssessment()

  @Transactional
  fun getCurrentAssessmentWithEligibilityProgress(prisonNumber: String): AssessmentWithEligibilityProgress {
    val offender = getOffender(prisonNumber)

    val currentAssessment = offender.currentAssessment()
    val policy = policyService.getVersionFromPolicy(currentAssessment.policyVersion)
    return AssessmentWithEligibilityProgress(
      assessmentEntity = currentAssessment,
      policy = policy,
    )
  }

  @Transactional
  fun getCurrentAssessmentSummary(prisonNumber: String): AssessmentSummary {
    val offender = getOffender(prisonNumber)
    return offenderToAssessmentSummaryMapper.map(offender)
  }

  @Transactional
  fun transitionAssessment(assessmentEntity: Assessment, event: AssessmentLifecycleEvent, agentDto: AgentDto?) {
    assessmentEntity.performTransition(event, agentDto.toEntity())
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun postponeCase(prisonNumber: String, postponeCaseRequest: PostponeCaseRequest) {
    val assessmentEntity = getCurrentAssessment(prisonNumber)

    assessmentEntity.performTransition(AssessmentLifecycleEvent.Postpone(postponeCaseRequest.reasonTypes), postponeCaseRequest.agentDto.toEntity())

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
    assessmentEntity.performTransition(OptOut(optOutRequest.reasonType, optOutRequest.otherDescription), optOutRequest.agentDto.toEntity())
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
  fun updateAddressChecksStatus(prisonNumber: String, status: ResidentialChecksStatus, saveTaskAnswersRequest: SaveResidentialChecksTaskAnswersRequest) {
    val event = AssessmentLifecycleEvent.ResidentialCheckStatusAnswerProvided(status, saveTaskAnswersRequest.taskCode, saveTaskAnswersRequest.answers)

    val assessmentEntity = getCurrentAssessment(prisonNumber)
    assessmentEntity.performTransition(event, saveTaskAnswersRequest.agentDto.toEntity())

    if (status == ResidentialChecksStatus.SUITABLE && !assessmentEntity.addressChecksComplete) {
      assessmentEntity.addressChecksComplete = true
    } else if (status != ResidentialChecksStatus.SUITABLE && assessmentEntity.addressChecksComplete) {
      assessmentEntity.addressChecksComplete = false
    }
    assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun updateTeamForResponsibleCom(staffCode: String, team: String) {
    var comsAssessments = assessmentRepository.findByResponsibleComStaffCodeAndStatusIn(staffCode, AssessmentStatus.inFlightStatuses())
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
      agentDto = eligibilityCheckResult?.agent?.toModel(),
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
}
