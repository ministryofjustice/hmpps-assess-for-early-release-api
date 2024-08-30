package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

fun interface EventProcessingCompleteHandler {
  fun complete()
}

val NO_OP = EventProcessingCompleteHandler { }
