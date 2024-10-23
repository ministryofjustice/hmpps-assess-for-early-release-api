package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

enum class Task {
  ASSESS_ELIGIBILITY,
  ENTER_CURFEW_ADDRESS,
  COMPLETE_PRE_RELEASE_CHECKS,
  REVIEW_APPLICATION_AND_SEND_FOR_DECISION,
  PREPARE_FOR_RELEASE,
  PRINT_LICENCE,
}

enum class TaskStatus {
  LOCKED,
  READY_TO_START,
  IN_PROGRESS,
  COMPLETE,
}
