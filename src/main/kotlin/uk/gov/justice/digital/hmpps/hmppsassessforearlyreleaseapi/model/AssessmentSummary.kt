package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import java.time.LocalDate

@Schema(description = "Response object which describes an assessment")
data class AssessmentSummary(
  @Schema(description = "The offender's first name", example = "Bob")
  val forename: String?,

  @Schema(description = "The offender's surname", example = "Smith")
  val surname: String?,

  @Schema(description = "The offender's date of birth", example = "15/04/2011")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate,

  @Schema(description = "The offender's prison number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "22/11/2026")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val hdced: LocalDate,

  @Schema(description = "The offender's conditional release date", example = "22/11/2026")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val crd: LocalDate?,

  @Schema(description = "The name of the prison the offender is in", example = "Foston Hall (HMP)")
  val location: String,

  @Schema(description = "The assessment status", example = "NOT_STARTED")
  val status: AssessmentStatus,

  @Schema(description = "The community offender manager assigned to this assessment")
  val responsibleCom: ComSummary? = null,

  @Schema(description = "The team the offender this assessment is assigned to", example = "N55LAU")
  val team: String? = null,

  @Schema(description = "The version of the policy that this assessment has been carried out under", example = "1.0")
  val policyVersion: String,

  @Schema(description = "The status of tasks that make up this assessment")
  val tasks: Map<UserRole, List<TaskProgress>>,
)
