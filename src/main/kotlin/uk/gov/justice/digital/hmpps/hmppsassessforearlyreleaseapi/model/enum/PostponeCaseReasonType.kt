package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum

enum class PostponeCaseReasonType(val description: String) {
  PLANNING_ACTIONS_CONFIRMATION_NEEDED_BY_PRACTITIONER("have outstanding risk management planning actions"),
  ON_REMAND("are on remand"),
  SEGREGATED_AND_GOVERNOR_NEEDS_TO_APPROVE_RELEASE("are segregated"),
  NEEDS_TO_SPEND_7_DAYS_IN_NORMAL_LOCATION_AFTER_SEGREGATION("have not spent 7 days in their normal location after being segregated"),
  PENDING_APPLICATION_WITH_UNDULY_LENIENT_SENTENCE_SCH("have an application pending with the unduly lenient sentence scheme"),
  WAITING_FOR_CAS2_ACCOMMODATION_OR_APPROVED_PREMISES_PLACEMENT("waiting for CAS-2 accommodation or Approved Premises placement"),
  WOULD_NOT_FOLLOW_REQUIREMENTS_OF_CONFISCATION_ORDER("would not follow the requirements of a confiscation order"),
  BEING_INVESTIGATED_FOR_OFFENCE_COMMITTED_IN_PRISON("are being investigated for an offence committed in prison"),
  ;

  companion object {
    fun getDescription(reason: PostponeCaseReasonType): String = reason.description
  }
}
