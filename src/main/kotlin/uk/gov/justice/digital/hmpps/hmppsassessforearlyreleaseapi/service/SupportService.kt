package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentEventResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentSearchResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.enums.TelemetryEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.AssessmentToAssessmentResponseMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToOffenderResponseMapper
import java.time.LocalDateTime

@Service
class SupportService(
  private val assessmentRepository: AssessmentRepository,
  private val offenderRepository: OffenderRepository,
  private val telemetryClient: TelemetryClient,
  private val assessmentService: AssessmentService,
  private val offenderResponseMapper: OffenderToOffenderResponseMapper,
  private val assessmentToAssessmentResponseMapper: AssessmentToAssessmentResponseMapper,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun searchForOffender(searchString: String): List<OffenderSearchResponse> {
    val offenders = offenderRepository.searchForOffender(searchString)
    log.debug("Found ${offenders.size} offenders")
    return offenders
  }

  fun getOffender(prisonNumber: String): OffenderResponse {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw ItemNotFoundException("Could not find offender: $prisonNumber")

    return offenderResponseMapper.map(offender)
  }

  fun getAssessments(prisonNumber: String): List<AssessmentSearchResponse> {
    val assessments = assessmentRepository.findByOffenderPrisonNumberOrderById(prisonNumber)
    log.debug("Retrieved ${assessments.size} assessments using prisonNumber: $prisonNumber")
    return assessments
  }

  @Transactional
  fun getAssessment(assessmentID: Long): AssessmentResponse {
    val assessment = assessmentRepository.findById(assessmentID).orElseThrow { ItemNotFoundException("Cannot find assessment with assessment id $assessmentID") }
    log.debug("Found assessment ${assessment.id}")
    return assessmentToAssessmentResponseMapper.map(assessment)
  }

  @Transactional
  fun getCurrentAssessment(prisonNumber: String): AssessmentResponse {
    val assessment = assessmentService.getCurrentAssessment(prisonNumber)
    log.debug("Found assessment ${assessment.id}")
    return assessmentToAssessmentResponseMapper.map(assessment)
  }

  @Transactional
  fun deleteAssessment(assessmentId: Long, agent: AgentDto) {
    log.debug("Deleting assessment for assessmentId {}", assessmentId)
    val assessment = assessmentRepository.findById(assessmentId).orElseThrow { ItemNotFoundException("Cannot find assessment with assessment id $assessmentId") }
    delete(assessment, agent)
  }

  @Transactional
  fun deleteCurrentAssessment(prisonerNumber: String, agent: AgentDto) {
    log.debug("Deleting current assessment for prisonerNumber: {}", prisonerNumber)
    val assessment = assessmentService.getCurrentAssessment(prisonerNumber)
    delete(assessment, agent)
  }

  fun getAssessmentEvents(assessmentId: Long, filter: List<AssessmentEventType>?): List<AssessmentEventResponse> = assessmentService.getAssessmentEvents(assessmentId, filter)

  private fun delete(
    assessment: Assessment,
    agent: AgentDto,
  ) {
    if (assessment.deletedTimestamp != null) {
      throw ValidationException("assessment already deleted")
    }

    val offender = assessment.offender
    val prisonNumber = offender.prisonNumber
    assessment.deletedTimestamp = LocalDateTime.now()

    val assessmentEventInfo = mutableMapOf(
      "prisonerNumber" to prisonNumber,
    )

    recordAssessmentEvent(AssessmentEventType.ASSESSMENT_DELETED, assessment, assessmentEventInfo, agent)

    val telemetryInfo = assessmentEventInfo + mapOf(
      "agent" to agent.username,
      "agentRole" to agent.role.name,
      "id" to assessment.id.toString(),
    )

    sendTelemetryInfo(telemetryInfo, TelemetryEvent.ASSESSMENT_DELETE_EVENT_NAME)

    assessmentRepository.save(assessment)

    val newAssessment =
      assessmentService.createAssessment(
        offender,
        prisonerNumber = prisonNumber,
        assessment.bookingId,
        assessment.hdced,
        assessment.crd,
        assessment.sentenceStartDate,
      )
    offender.assessments.add(newAssessment)
  }

  private fun recordAssessmentEvent(
    type: AssessmentEventType,
    assessment: Assessment,
    info: MutableMap<String, String>,
    agent: AgentDto,
  ) {
    assessment.recordEvent(
      eventType = type,
      info,
      agent = agent.toEntity(),
    )
  }

  private fun sendTelemetryInfo(
    deleteInfo: Map<String, String>,
    telemetryEvent: TelemetryEvent,
  ) {
    telemetryClient.trackEvent(
      telemetryEvent.key,
      deleteInfo,
      null,
    )
  }
}
