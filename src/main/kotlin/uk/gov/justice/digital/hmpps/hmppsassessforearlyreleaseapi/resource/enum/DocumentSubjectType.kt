package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum

enum class DocumentSubjectType(val includesSignAndName: Boolean, val includesGradeAndDate: Boolean) {
  OFFENDER_NOT_ELIGIBLE_FORM(true, true),
  OFFENDER_POSTPONED_FORM(true, true),
  OFFENDER_REFUSED_FORM(true, true),
  OFFENDER_ELIGIBLE_FORM(false, false),
  OFFENDER_ADDRESS_CHECKS_INFORMATION_FORM(false, false),
  OFFENDER_ADDRESS_CHECKS_FORM(false, false),
  OFFENDER_OPT_OUT_FORM(true, false),
  OFFENDER_ADDRESS_UNSUITABLE_FORM(false, false),
  OFFENDER_NOT_ENOUGH_TIME_FORM(false, false),
  OFFENDER_APPROVED_FORM(false, false),
  OFFENDER_AGENCY_NOTIFICATION_FORM(false, false),
  OFFENDER_CANCEL_AGENCY_NOTIFICATION_FORM(false, false),
  OFFENDER_NOT_SUITABLE_FORM(false, false),
}
