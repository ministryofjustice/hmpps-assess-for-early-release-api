package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

data class DeliusOffenderManager(
  val id: Long,
  val code: String,
  val name: Name,
  val team: Team,
  val provider: Provider,
  val username: String? = null,
  val email: String? = null,
)

data class Name(val forename: String, val middleName: String? = null, val surname: String)

data class Provider(val code: String, val description: String)

data class Team(
  val code: String,
  val description: String,
  val borough: Borough? = null,
  val district: District? = null,
)

data class Borough(val code: String, val description: String)
data class District(val code: String, val description: String)
