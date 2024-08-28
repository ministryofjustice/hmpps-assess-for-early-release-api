package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import java.time.LocalDateTime

@Service
class OffenderService(
  private val offenderRepository: OffenderRepository,
  private val prisonerSearchService: PrisonerSearchService,
) {
  @Transactional
  fun createOrUpdateOffender(prisonerNumber: String) {
    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(listOf(prisonerNumber))
    if (prisoners.isNotEmpty()) {
      val prisoner = prisoners.first()
      if (prisoner.homeDetentionCurfewEligibilityDate != null) {
        val offender = offenderRepository.findByPrisonerNumber(prisonerNumber)
        if (offender != null) {
          updateOffender(offender, prisoner)
        } else {
          createOffender(prisoner)
        }
      }
    } else {
      val msg = "Could not find prisoner with prisonerNumber $prisonerNumber in prisoner search"
      log.warn(msg)
      throw Exception(msg)
    }
  }

  private fun createOffender(prisoner: PrisonerSearchPrisoner) {
    val offender = Offender(
      bookingId = prisoner.bookingId!!.toLong(),
      prisonerNumber = prisoner.prisonerNumber,
      prisonId = prisoner.prisonId!!,
      firstName = prisoner.firstName,
      lastName = prisoner.lastName,
      hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
    )
    offenderRepository.save(offender)
  }

  private fun updateOffender(offender: Offender, prisoner: PrisonerSearchPrisoner) {
    val updatedOffender = offender.copy(
      bookingId = prisoner.bookingId!!.toLong(),
      prisonId = prisoner.prisonId!!,
      firstName = prisoner.firstName,
      lastName = prisoner.lastName,
      hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
      lastUpdatedTimestamp = LocalDateTime.now(),
    )
    offenderRepository.save(updatedOffender)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
