package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for updating the VLO and POM consultation information for an assessment")
data class UpdateVloAndPomConsultationRequest(
  @Schema(description = "Does the case qualify for and has the victim opted in for the Victim Contact Scheme", example = "true")
  val victimContactSchemeOptedIn: Boolean,

  @Schema(description = "Details of any requests the victim has made", example = "Any exclusion zones that have been requested")
  val victimContactSchemeRequests: String? = null,

  @Schema(description = "Information that the POM has provided about the offender's behaviour in prison", example = "Any concerns about them being released on HDC")
  val pomBehaviourInformation: String? = null,
)
