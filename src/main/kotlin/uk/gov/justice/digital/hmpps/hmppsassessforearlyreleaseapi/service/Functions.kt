package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

private fun properCase(word: String) =
  if (word.isNotEmpty()) word[0].uppercase() + word.lowercase().substring(1) else word

/**
 * Converts a name (first name, last name, middle name, etc.) to proper case equivalent, handling double-barreled names
 * correctly (i.e. each part in a double-barreled is converted to proper case).
 */
private fun properCaseName(name: String) = if (name.isBlank()) "" else name.split('-').joinToString("-") { properCase(it) }

fun String.convertToTitleCase() = if (this.isBlank()) "" else this.split(" ").joinToString(" ") { properCaseName(it) }
