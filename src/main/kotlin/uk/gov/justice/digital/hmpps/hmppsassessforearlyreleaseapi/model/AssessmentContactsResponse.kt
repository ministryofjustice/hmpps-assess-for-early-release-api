package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A response to contain assessment contacts")
data class AssessmentContactsResponse(

  @Schema(description = "a set of contacts associated with the assessment")
  val contacts: List<ContactResponse> = emptyList(),
)
