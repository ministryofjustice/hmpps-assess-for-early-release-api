package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus
import java.time.LocalDateTime

enum class ResidentialChecksTaskAnswerType(val taskCode: String, val taskAnswerClass: Class<out AnswerPayload>) {
  ADDRESS_DETAILS_AND_INFORMED_CONSENT("address-details-and-informed-consent", AddressDetailsAnswers::class.java),
  POLICE_CHECK("police-check", PoliceChecksAnswers::class.java),
  CHILDREN_SERVICES_CHECK("children-services-check", ChildrenServicesChecksAnswers::class.java),
  ASSESS_THIS_PERSONS_RISK("assess-this-persons-risk", AssessPersonsRiskAnswers::class.java),
  SUITABILITY_DECISION("suitability-decision", SuitabilityDecisionAnswers::class.java),
  MAKE_A_RISK_MANAGEMENT_DECISION("make-a-risk-management-decision", RiskManagementDecisionAnswers::class.java),
  ;

  companion object {
    fun getByTaskCode(taskCode: String): ResidentialChecksTaskAnswerType = entries.find { it.taskCode == taskCode } ?: throw IllegalArgumentException("Invalid task code")
  }
}

sealed interface AnswerPayload {
  fun createTaskAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    criterionMet: Boolean,
    taskVersion: String,
  ): ResidentialChecksTaskAnswer
}

@Entity
@Table(name = "residential_checks_task_answer")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "task_code", discriminatorType = DiscriminatorType.STRING)
abstract class ResidentialChecksTaskAnswer(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_check_request_id", nullable = false)
  val addressCheckRequest: CurfewAddressCheckRequest,

  @NotNull
  @Column(name = "task_code", insertable = false, updatable = false)
  val taskCode: String,

  @NotNull
  @Column(name = "criterion_met")
  var criterionMet: Boolean,

  @NotNull
  @Column(name = "task_version")
  val taskVersion: String,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  var lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) {
  abstract fun getAnswers(): AnswerPayload
  abstract fun updateAnswers(answers: AnswerPayload): ResidentialChecksTaskAnswer
  abstract fun toAnswersMap(): Map<String, Any?>
}

fun ResidentialChecksTaskAnswer?.status(): TaskStatus {
  if (this == null || this.toAnswersMap().isEmpty()) {
    return TaskStatus.NOT_STARTED
  }
  return if (criterionMet) TaskStatus.SUITABLE else TaskStatus.UNSUITABLE
}
