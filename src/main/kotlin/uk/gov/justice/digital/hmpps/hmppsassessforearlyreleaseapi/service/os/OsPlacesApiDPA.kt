package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class OsPlacesApiDPA(
  @JsonProperty("UPRN")
  val uprn: String,

  @JsonProperty("ADDRESS")
  val address: String,

  @JsonProperty("ORGANISATION_NAME")
  val organisationName: String?,

  @JsonProperty("BUILDING_NAME")
  val buildingName: String?,

  @JsonProperty("BUILDING_NUMBER")
  val buildingNumber: String?,

  @JsonProperty("THOROUGHFARE_NAME")
  val thoroughfareName: String?,

  @JsonProperty("DEPENDENT_LOCALITY")
  val locality: String?,

  @JsonProperty("POST_TOWN")
  val postTown: String,

  @JsonProperty("LOCAL_CUSTODIAN_CODE_DESCRIPTION")
  val county: String,

  @JsonProperty("POSTCODE")
  val postcode: String,

  @JsonProperty("COUNTRY_CODE_DESCRIPTION")
  val countryDescription: String,

  @JsonProperty("X_COORDINATE")
  val xCoordinate: Double,

  @JsonProperty("Y_COORDINATE")
  val yCoordinate: Double,

  @JsonFormat(pattern = "dd/MM/yyyy")
  @JsonProperty("LAST_UPDATE_DATE")
  val lastUpdateDate: LocalDate,
)
