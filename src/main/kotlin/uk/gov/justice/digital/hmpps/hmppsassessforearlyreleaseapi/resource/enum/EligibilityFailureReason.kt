package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum

enum class EligibilityFailureReason(val reason: String) {
  SEXUAL_OFFENDING_HISTORY("of your conviction history"),
  FINANCIAL_FRAUD("due to financial fraud"),
  IDENTITY_THEFT("because of identity theft"),
  SEX_OFFENDER_REGISTER("because you are on the sex offender register"),
  ROTL_FAILURE_TO_RETURN("due to ROTL failure to return"),
  OTHER("for other reasons");

  companion object {
    fun getFailureReason(code: String): String {
      return when (code) {
        "Sex offenders' register" -> SEX_OFFENDER_REGISTER.reason
        "ROTL failure to return" -> ROTL_FAILURE_TO_RETURN.reason
        else -> values().find { it.name.equals(code, ignoreCase = true) }?.reason ?: "No failure reason"
      }
    }
  }
}
