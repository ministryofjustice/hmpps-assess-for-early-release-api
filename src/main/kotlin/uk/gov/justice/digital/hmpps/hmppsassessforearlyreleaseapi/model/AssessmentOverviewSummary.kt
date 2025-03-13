package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import java.time.LocalDate

@Schema(description = "Response object which describes an assessment")
data class AssessmentOverviewSummary(
  @Schema(description = "The offender's first name", example = "Bob")
  val forename: String?,

  @Schema(description = "The offender's surname", example = "Smith")
  val surname: String?,

  @Schema(description = "The offender's date of birth", example = "2002-02-20")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate,

  @Schema(description = "The offender's prison number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "2002-02-20")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val hdced: LocalDate,

  @Schema(description = "The offender's conditional release date", example = "2002-02-20")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val crd: LocalDate?,

  @Schema(description = "The name of the prison the offender is in", example = "Foston Hall (HMP)")
  val location: String,

  @Schema(description = "The assessment status", example = "NOT_STARTED")
  val status: AssessmentStatus,

  @Schema(description = "The community offender manager assigned to this assessment")
  val responsibleCom: ComSummary? = null,

  @Schema(description = "The team that the COM responsible for this assessment is assigned to", example = "N55LAU")
  val team: String? = null,

  @Schema(description = "The version of the policy that this assessment has been carried out under", example = "1.0")
  val policyVersion: String,

  @Schema(description = "The status of tasks that make up this assessment")
  val tasks: Map<UserRole, List<TaskProgress>>,

  @Schema(description = "The opt out reason type")
  var optOutReasonType: OptOutReasonType? = null,

  @Schema(description = "The opt out reason description if rhe optOutReasonType is OTHER")
  var optOutReasonOther: String? = null,

  @Schema(description = "Prisoner cell location", example = "A-1-002", required = false)
  val cellLocation: String?,

  @Schema(description = "The main offense also know as the most serious offence", example = "Robbery", required = false)
  val mainOffense: String?,

  @Schema(description = "The assessment created date plus five days", example = "2002-02-20")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val toDoEligibilityAndSuitabilityBy: LocalDate?,

  @Schema(description = "The assessment's assess eligibility and suitability task result", example = "Ineligible")
  val result: String?,
)
