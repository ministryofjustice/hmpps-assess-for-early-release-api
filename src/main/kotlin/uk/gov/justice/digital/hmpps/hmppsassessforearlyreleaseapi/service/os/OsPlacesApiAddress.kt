package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os

import com.fasterxml.jackson.annotation.JsonProperty

data class OsPlacesApiAddress(
  @JsonProperty("DPA")
  val dpa: OsPlacesApiDPA,
)
