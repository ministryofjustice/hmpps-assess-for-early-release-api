package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import java.time.LocalDate

@Schema(description = "Response object which describes an assessment")
data class AssessmentSummary(

  @Schema(description = "The offender's booking id", example = "773722")
  val bookingId: Long,

  @Schema(description = "The offender's first name", example = "Kalitta")
  val forename: String?,

  @Schema(description = "The offender's surname", example = "Amexar")
  val surname: String?,

  @Schema(description = "The offender's date of birth", example = "2002-02-20")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val dateOfBirth: LocalDate,

  @Schema(description = "The offender's prison number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "The offender's home detention curfew eligibility date", example = "2002-02-20")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val hdced: LocalDate,

  @Schema(description = "The offender's conditional release date", example = "2002-02-20")
  @JsonFormat(pattern = API_DATE_FORMAT)
  val crd: LocalDate?,

  @Schema(description = "The name of the prison the offender is in", example = "Foston Hall (HMP)")
  val location: String,

  @Schema(description = "The assessment status", example = "NOT_STARTED")
  val status: AssessmentStatus,

  @Schema(description = "The community offender manager assigned to this assessment")
  val responsibleCom: ComSummary? = null,

  @Schema(description = "The team that the COM responsible for this assessment is assigned to", example = "Team1")
  val teamCode: String? = null,

  @Schema(description = "The version of the policy that this assessment has been carried out under", example = "1.0")
  val policyVersion: String,

  @Schema(description = "The status of tasks that make up this assessment")
  val tasks: Map<UserRole, List<TaskProgress>>,

  @Schema(description = "The opt out reason type")
  var optOutReasonType: OptOutReasonType? = null,

  @Schema(description = "The opt out reason description if rhe optOutReasonType is OTHER")
  var optOutReasonOther: String? = null,

  @Schema(description = "The reasons that the offender's current assessment was postponed", example = "ON_REMAND")
  val postponementReasons: List<PostponeCaseReasonType> = listOf(),

  @Schema(description = "Prisoner cell location", example = "A-1-002", required = false)
  val cellLocation: String?,

  @Schema(description = "The main offense also know as the most serious offence", example = "Robbery", required = false)
  val mainOffense: String?,

  @Schema(description = "Last updated by", example = "Bura Hurn")
  val lastUpdateBy: String? = null,
)
