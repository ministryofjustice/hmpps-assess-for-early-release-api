package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import com.tinder.StateMachine.Transition.Invalid
import com.tinder.StateMachine.Transition.Valid
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToOne
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.GenericChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.StatusChange
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.StatusChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.staff.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentLifecycleEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.Companion.toState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.SideEffect
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.assessmentStateMachine
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import java.time.LocalDate
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent as AgentEntity

@Entity
@NamedEntityGraph(name = "Assessment.offender", attributeNodes = [NamedAttributeNode("offender")])
@Table(name = "assessment")
data class Assessment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @NotNull
  val bookingId: Long,

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "offender_id", nullable = false)
  val offender: Offender,

  @NotNull
  @Enumerated(EnumType.STRING)
  var status: AssessmentStatus = AssessmentStatus.NOT_STARTED,

  @NotNull
  @Enumerated(EnumType.STRING)
  var previousStatus: AssessmentStatus? = null,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  var lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),

  var deletedTimestamp: LocalDateTime? = null,

  @NotNull
  val policyVersion: String = "???",

  @NotNull
  @Column(name = "address_checks_complete")
  var addressChecksComplete: Boolean = false,

  @NotNull
  @Enumerated(EnumType.STRING)
  var addressChecksStatus: ResidentialChecksStatus = ResidentialChecksStatus.NOT_STARTED,

  @NotNull
  @Enumerated(EnumType.STRING)
  var eligibilityChecksStatus: EligibilityStatus = EligibilityStatus.NOT_STARTED,

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val eligibilityCheckResults: MutableSet<EligibilityCheckResult> = mutableSetOf(),

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @OrderBy("eventTime DESC")
  private val assessmentEvents: MutableList<AssessmentEvent> = mutableListOf(),

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "responsible_com_id")
  var responsibleCom: CommunityOffenderManager? = null,

  val teamCode: String? = null,

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @OrderBy("createdTimestamp ASC")
  val postponementReasons: MutableList<PostponementReasonEntity> = mutableListOf(),

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinTable(
    name = "assessment_to_last_update_event",
    joinColumns =
    [JoinColumn(name = "assessment_id", referencedColumnName = "id")],
    inverseJoinColumns =
    [JoinColumn(name = "event_id", referencedColumnName = "id")],
  )
  var lastUpdateByUserEvent: AssessmentEvent? = null,

  var postponementDate: LocalDate? = null,

  @Enumerated(EnumType.STRING)
  var optOutReasonType: OptOutReasonType? = null,

  var optOutReasonOther: String? = null,

  var hasNonDisclosableInformation: Boolean? = null,

  var nonDisclosableInformation: String? = null,

  var victimContactSchemeOptedIn: Boolean? = null,

  var victimContactSchemeRequests: String? = null,

  var pomBehaviourInformation: String? = null,
) {
  @Override
  override fun toString(): String = this::class.simpleName + "(id: $id, status: $status)"

  @Override
  override fun equals(other: Any?): Boolean {
    if (this === other) return true

    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as Assessment
    return id == other.id
  }

  fun addOrReplaceEligibilityCriterionResult(
    criterionType: CriterionType,
    criterionCode: String,
    criterionMet: Boolean,
    answers: Map<String, Boolean>,
    agent: AgentDto,
  ) {
    val currentResults = this.eligibilityCheckResults

    val existingCriterionResult =
      currentResults.find { it.criterionType == criterionType && it.criterionCode == criterionCode }

    val criteria = when {
      existingCriterionResult != null -> {
        currentResults.remove(existingCriterionResult)
        existingCriterionResult.copy(
          criterionMet = criterionMet,
          questionAnswers = answers,
          lastUpdatedTimestamp = LocalDateTime.now(),
          agent = agent.toEntity(),
        )
      }

      else -> EligibilityCheckResult(
        assessment = this,
        criterionMet = criterionMet,
        questionAnswers = answers,
        criterionCode = criterionCode,
        criterionVersion = this.policyVersion,
        criterionType = criterionType,
        agent = agent.toEntity(),
      )
    }

    currentResults.add(criteria)
  }

  private fun Any.label() = this::class.simpleName

  fun performTransition(
    lifecycleEvent: AssessmentLifecycleEvent,
    agent: AgentEntity,
  ): AssessmentState {
    val currentStatus = this.status.toState(this.previousStatus)
    val transition = assessmentStateMachine.with { initialState(currentStatus) }.transition(lifecycleEvent)
    return when (transition) {
      is Invalid -> {
        error("Fail to transition Assessment: '${this.id}', triggered by '${transition.event.label()}' from '${transition.fromState.label()}'")
      }

      is Valid -> {
        transition.sideEffect
          ?.takeIf { it is SideEffect.Error }
          ?.run { error((this as SideEffect.Error).message) }

        log.info("Transitioning Assessment: '${this.id}', triggered by event: '${transition.event.label()}' from '${transition.fromState.label()}' to '${transition.toState.label()}'")
        if (currentStatus != transition.toState) {
          val statusEvent = StatusChangedEvent(
            assessment = this,
            changes = StatusChange(
              before = transition.fromState.status,
              after = transition.toState.status,
              context = lifecycleEvent.getContext(),
            ),
            agent = agent,
          )

          this.previousStatus = currentStatus.status
          this.status = transition.toState.status

          addEvent(statusEvent, agent)
        }
        transition.toState
      }
    }
  }

  fun recordEvent(eventType: AssessmentEventType, changes: Map<String, Any>, agent: AgentEntity) {
    val genericChangedEvent = GenericChangedEvent(
      assessment = this,
      changes = changes,
      eventType = eventType,
      agent = agent,
    )
    this.addEvent(genericChangedEvent, agent)
  }

  fun addEvent(
    event: AssessmentEvent,
    agent: Agent,
  ) {
    assessmentEvents.add(event)
    lastUpdatedTimestamp = LocalDateTime.now()
    if (agent.role != UserRole.SYSTEM) {
      lastUpdateByUserEvent = event
    }
  }

  fun getEvents(): List<AssessmentEvent> = this.assessmentEvents

  fun tasks(): Map<UserRole, List<TaskProgress>> = Task.TASKS_BY_ROLE.mapValues { (_, tasks) ->
    tasks.mapNotNull {
      it.taskStatus(this)?.let { taskStatus ->
        TaskProgress(it, taskStatus)
      }
    }
  }

  fun currentTask(): Task? {
    val tasksForAssessmentStatus = Task.TASKS_BY_ROLE.values.flatten()
    val availableTasks = tasksForAssessmentStatus.filter {
      val taskStatus = it.taskStatus(this)
      taskStatus == READY_TO_START || taskStatus == IN_PROGRESS
    }

    val nextTask = availableTasks.firstOrNull { it.taskStatus(this) == READY_TO_START }
      ?: availableTasks.firstOrNull { it.taskStatus(this) == IN_PROGRESS }
    return nextTask
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
