package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum

enum class PostponeCaseReasonType(val description: String) {
  PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER("planning actions confirmation needed by practitioner"),
  ON_REMAND("are on remand"),
  SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE("are segregated"),
  NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION("have not spent 7 days in their normal location after being segregated"),
  COMMITED_OFFENCE_REFERRED_TO_LAW_ENF_AGENCY("have committed an offence that has been referred to a law enforcement agency"),
  CONFISCATION_ORDER_NOT_PAID_AND_ENF_AGENCY_DEEMS_UNSUITABLE("would not follow the requirements of a confiscation order"),
  PENDING_APPLICATION_WITH_UNDULY_LENIENT_LENIENT_SCH("have an application pending with the unduly lenient sentence scheme"),
  OUTSTANDING_RISK_MANAGEMENT_PLANNING_ACTIONS("have outstanding risk management planning actions"),
  WAITING_FOR_CAS2_ACCOMMODATION_OR_APPROVED_PREMISES_PLACEMENT("waiting for CAS-2 accommodation or Approved Premises placement"),
  WOULD_NOT_FOLLOW_REQUIREMENTS_OF_CONFISCATION_ORDER("would not follow the requirements of a confiscation order"),
  BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON("are being investigated for an offence committed in prison"),
  ;

  companion object {
    fun getDescription(reason: PostponeCaseReasonType): String = reason.description
  }
}
