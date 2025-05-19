package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressDeletionEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.RESIDENTIAL_CHECKS_POLICY_V1
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.UNSUITABLE
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class CurfewAddressCheckRequest(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  var caAdditionalInfo: String? = null,

  val ppAdditionalInfo: String? = null,

  @NotNull
  val dateRequested: LocalDateTime = LocalDateTime.now(),

  @NotNull
  @Enumerated(EnumType.STRING)
  val preferencePriority: AddressPreferencePriority,

  @NotNull
  @Enumerated(EnumType.STRING)
  val status: AddressCheckRequestStatus,

  @ManyToOne
  @JoinColumn(name = "assessment_id", referencedColumnName = "id", nullable = false)
  val assessment: uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment,

  @OneToMany(mappedBy = "addressCheckRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
  val taskAnswers: MutableSet<ResidentialChecksTaskAnswer> = mutableSetOf(),

  @OneToOne(cascade = [CascadeType.ALL])
  @JoinColumn(name = "address_deletion_event_id", referencedColumnName = "id")
  var addressDeletionEvent: AddressDeletionEvent? = null,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) {

  private fun getPolicyTaskCodes(): List<String> = RESIDENTIAL_CHECKS_POLICY_V1.tasks.map { it.code }

  private fun ResidentialChecksTaskAnswer?.toTaskStatus(): TaskStatus = when (this?.criterionMet) {
    null -> NOT_STARTED
    true -> SUITABLE
    false -> UNSUITABLE
  }

  fun getStatus(taskAnswers: MutableSet<ResidentialChecksTaskAnswer>): TaskStatus {
    val taskStatuses = getPolicyTaskCodes().map { taskCode ->
      taskAnswers.find { it.taskCode == taskCode }?.toTaskStatus()
    }

    return when {
      taskStatuses.all { it == SUITABLE } -> SUITABLE
      taskStatuses.any { it == UNSUITABLE } -> UNSUITABLE
      taskStatuses.any { it == SUITABLE } -> IN_PROGRESS
      else -> NOT_STARTED
    }
  }
}
