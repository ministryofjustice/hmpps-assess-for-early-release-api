package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum

enum class DocumentSubjectType(val includesGradeAndDate: Boolean) {
  OFFENDER_NOT_ELIGIBLE_FORM(true),
  OFFENDER_POSTPONED_FORM(true),
  OFFENDER_REFUSED_FORM(true),
  OFFENDER_ELIGIBLE_FORM(false),
  OFFENDER_ADDRESS_CHECKS_INFORMATION_FORM(false),
  OFFENDER_ADDRESS_CHECKS_FORM(false),
  OFFENDER_OPT_OUT_FORM(false),
  OFFENDER_ADDRESS_UNSUITABLE_FORM(false),
  OFFENDER_NOT_ENOUGH_TIME_FORM(false),
  OFFENDER_APPROVED_FORM(false),
  OFFENDER_AGENCY_NOTIFICATION_FORM(false),
  OFFENDER_CANCEL_AGENCY_NOTIFICATION_FORM(false),
  OFFENDER_NOT_SUITABLE_FORM(false),
}
