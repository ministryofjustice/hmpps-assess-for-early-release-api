package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.enums

enum class TelemetryEvent(val key: String) {
  PRISONER_CREATED_EVENT_NAME("assess-for-early-release.prisoner.created"),
  PRISONER_UPDATED_EVENT_NAME("assess-for-early-release.prisoner.updated"),
  ASSESSMENT_DELETE_EVENT_NAME("assess-for-early-release.assessment.delete"),
}
