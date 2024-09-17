package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.EligibilityCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Policy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.SuitabilityCheck

val POLICY_1_0 = Policy(
  code = "1.0",
  eligibilityCriteria = listOf(
    EligibilityCheck.YesNo(
      code = "sex-offender-register",
      name = "Sex offenders' register",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "extended-sentence-for-violent-or-sexual-offences",
      name = "Extended Sentence for violent or sexual offences",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "rotl-failure-to-return",
      name = "ROTL failure to return",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "community-order-curfew-breach",
      name = "Community order curfew breach",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "recalled-for-breaching-hdc-curfew",
      name = "Recalled for breaching HDC curfew",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "deportation-orders",
      name = "Deportation Orders",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "recalled-from-early-release-on-compassionate-grounds",
      name = "Recalled from early release on compassionate grounds",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "terrorism",
      name = "Terrorism",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "two-thirds-release",
      name = "Two-thirds release",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "section-244ZB-notice",
      name = "Section 244ZB notice",
      question = "some question...",
    ),
    EligibilityCheck.YesNo(
      code = "section-20b-release",
      name = "Section 20B release",
      question = "some question...",
    ),

  ),

  suitabilityCriteria = listOf(
    SuitabilityCheck.YesNo(
      code = "sexual-offending-history",
      name = "Sexual offending history",
      question = "some question...",
    ),
    SuitabilityCheck.YesNo(
      code = "liable-to-deportation",
      name = "Liable to deportation",
      question = "some question...",
    ),
    SuitabilityCheck.YesNo(
      code = "recalled-for-poor-behaviour-on-hdc",
      name = "Recalled for poor behaviour on hdc",
      question = "some question...",
    ),
    SuitabilityCheck.YesNo(
      code = "category-a",
      name = "Category A",
      question = "some question...",
    ),
    SuitabilityCheck.YesNo(
      code = "rosh-and-mappa",
      name = "RoSH and MAPPA",
      question = "some question...",
    ),
    SuitabilityCheck.YesNo(
      code = "presumed-unsuitable-offences",
      name = "Presumed unsuitable offences",
      question = "some question...",
    ),
    SuitabilityCheck.YesNo(
      code = "terrorist-offending-history",
      name = "Terrorist offending history",
      question = "some question...",
    ),
  ),
)
