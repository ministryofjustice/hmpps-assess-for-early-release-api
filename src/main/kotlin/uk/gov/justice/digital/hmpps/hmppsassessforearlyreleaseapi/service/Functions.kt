package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
