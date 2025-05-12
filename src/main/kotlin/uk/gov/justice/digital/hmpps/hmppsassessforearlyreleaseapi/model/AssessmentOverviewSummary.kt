package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.API_DATE_FORMAT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas.CasAccommodationStatusInfoResponse
import java.time.LocalDate

@Schema(description = "Response object which describes an assessment")
data class AssessmentOverviewSummary(

  @Schema(description = "The offender's booking id", example = "773722")
  val bookingId: Long,

  @Schema(description = "The offender's first name", example = "Bob")
  val forename: String?,

  @Schema(description = "The offender's surname", example = "Smith")
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

  @Schema(description = "The team that the COM responsible for this assessment is assigned to", example = "N55LAU")
  val teamCode: String? = null,

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
  @JsonFormat(pattern = API_DATE_FORMAT)
  val toDoEligibilityAndSuitabilityBy: LocalDate?,

  @Schema(description = "The assessment's assess eligibility and suitability task result", example = "Ineligible")
  val result: String?,

  @Schema(description = "Indicates whether the prisoner's information is non-disclosable", example = "false")
  var hasNonDisclosableInformation: Boolean? = null,

  @Schema(description = "Reason why the prisoner's information is non-disclosable", example = "Security concerns")
  var nonDisclosableInformation: String? = null,

  @Schema(description = "Does the case qualify for and has the victim opted in for the Victim Contact Scheme", example = "true")
  val victimContactSchemeOptedIn: Boolean? = null,

  @Schema(description = "Details of any requests the victim has made", example = "Any exclusion zones that have been requested")
  val victimContactSchemeRequests: String? = null,

  @Schema(description = "Information that the POM has provided about the offender's behaviour in prison", example = "Any concerns about them being released on HDC")
  val pomBehaviourInformation: String? = null,

  @Schema(description = "Information On the current cas accommodation assessment")
  val currentCasAccommodationStatusInfo: CasAccommodationStatusInfoResponse? = null,

  @Schema(description = "Last updated by", example = "Aled Evans")
  val lastUpdateBy: String? = null,
)
