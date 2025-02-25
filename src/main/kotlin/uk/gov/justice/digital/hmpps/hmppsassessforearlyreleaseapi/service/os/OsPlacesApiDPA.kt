package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Address
import java.time.LocalDate

data class OsPlacesApiDPA(
  @JsonProperty("UPRN")
  val uprn: String,

  @JsonProperty("ADDRESS")
  val address: String,

  @JsonProperty("SUB_BUILDING_NAME")
  val subBuildingName: String? = null,

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

fun OsPlacesApiDPA.getAddressFirstLine(): String {
  var firstLine = if (this.organisationName != null) this.organisationName + ", " else ""
  firstLine += if (this.subBuildingName != null) this.subBuildingName + ", " else ""
  firstLine += if (this.buildingName != null) this.buildingName + ", " else ""
  firstLine += if (this.thoroughfareName.isNullOrEmpty()) {
    if (this.buildingNumber != null) this.buildingNumber + ", " + this.locality else this.locality
  } else {
    if (this.buildingNumber != null) this.buildingNumber + " " + this.thoroughfareName else this.thoroughfareName
  }
  return firstLine
}

fun OsPlacesApiDPA.toAddress(): Address = Address(
  uprn = this.uprn,
  firstLine = getAddressFirstLine(),
  secondLine = this.locality,
  town = this.postTown,
  county = this.county,
  postcode = this.postcode,
  country = this.countryDescription.split("\\s+".toRegex()).last(),
  xCoordinate = this.xCoordinate,
  yCoordinate = this.yCoordinate,
  addressLastUpdated = this.lastUpdateDate,
)
