package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Input
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.InputType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Option
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.PolicyVersion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Question
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksPolicy
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Section
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Task

val RESIDENTIAL_CHECKS_POLICY_V1 = ResidentialChecksPolicy(
  version = PolicyVersion.V1,
  tasks = listOf(
    Task(
      code = "address-details-and-informed-consent",
      name = "Address details and informed consent",
      sections = listOf(
        Section(
          questions = listOf(
            Question(
              code = "connected-to-an-electricity-supply",
              text = "Is the address connected to an electricity supply?",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
          ),
        ),
        Section(
          header = "Informed consent",
          questions = listOf(
            Question(
              code = "have-you-visited-this-address-in-person",
              text = "Have you visited this address in person?",
              hintText = "It is not mandatory to do so.",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("I have visited this address and spoken to the main occupier"),
                  Option("I have not visited the address but I have spoken to the main occupier"),
                ),
              ),
            ),
            Question(
              code = "main-occupier-given-consent",
              text = "Has the main occupier given informed consent for {offenderForename} to be released here?",
              hintText = """
                <p>They must understand</p>
                <ul class="govuk-list govuk-list--bullet">
                  <li>what HDC involves</li>
                  <li>the offences {offenderForename} committed</li>
                </ul>
              """.trimIndent(),
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
          ),
        ),
      ),
    ),
    Task(
      code = "police-check",
      name = "Police check",
      sections = listOf(
        Section(
          header = "Police checks",
          hintText = """
            You must request and consider information from the police about the domestic abuse and
            child wellbeing risks of releasing this person to the proposed address.
          """.trimIndent(),
          questions = listOf(
            Question(
              code = "date-police-information-requested",
              text = "Enter the date that you requested this information",
              hintText = "For example, 31 3 1980",
              input = Input(
                type = InputType.DATE,
              ),
            ),
            Question(
              code = "date-police-sent-information",
              text = "Enter the date that the police sent this information",
              hintText = "For example, 31 3 1980",
              input = Input(
                type = InputType.DATE,
              ),
            ),
            Question(
              code = "police-information-summary",
              text = "Summarise the information the police provided",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
          ),
        ),
      ),
    ),
    Task(
      code = "children-services-check",
      name = "Children's services check",
      sections = listOf(
        Section(
          header = "Children's services checks",
          hintText = """
            You must request and consider information from children's services about the domestic abuse and
            child wellbeing risks of releasing this person to the proposed address.
          """.trimIndent(),
          questions = listOf(
            Question(
              code = "date-children-services-information-requested",
              text = "Enter the date that you requested this information",
              hintText = "For example, 31 3 1980",
              input = Input(
                type = InputType.DATE,
              ),
            ),
            Question(
              code = "date-children-services-information-sent",
              text = "Enter the date that children's services sent this information",
              hintText = "For example, 31 3 1980",
              input = Input(
                type = InputType.DATE,
              ),
            ),
            Question(
              code = "children-services-information-summary",
              text = "Summarise the information children's services provided",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
          ),
        ),
      ),
    ),
    Task(
      code = "assess-this-persons-risk",
      name = "Assess this person's risk",
      sections = listOf(
        Section(
          header = "Risk management information",
          questions = listOf(
            Question(
              code = "pom-prison-behaviour-information",
              text = "What information has the POM provided about the behaviour of {offenderForename} while in prison?",
              hintText = "Find out if there are any concerns about them being released on HDC or if there have been any changes to their level of risk.",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
            Question(
              code = "mental-health-treatment-needs",
              text = "Does {offenderForename} need any mental health treatment to help manage risk?",
              hintText = "If so, it should be considered as part of your risk management planning actions.",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
            Question(
              code = "is-there-a-vlo-officer-for-case",
              text = "Is there a victim liaison officer (VLO) for this case?",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
            Question(
              code = "information-that-cannot-be-disclosed-to-offender",
              text = "Is there any information that cannot be disclosed to {offenderForename}?",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
          ),
        ),
      ),
    ),
    Task(
      code = "suitability-decision",
      name = "Suitability decision",
      sections = listOf(
        Section(
          header = "Suitability decision",
          questions = listOf(
            Question(
              code = "69b608d0-35e1-44ea-9982-84d1cf6c0045",
              text = "Is this address suitable for {offenderForename} to be released to?",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
            Question(
              code = "e7ac8d33-fc04-4660-9d0e-bf121acf703f",
              text = "Add information to support your decision",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
            Question(
              code = "084edb2b-a52b-4723-b425-0069719fd5f9",
              text = "Do you need to add any more information about {offenderForename} or the proposed address for the monitoring contractor?",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
            Question(
              code = "cf8ffa01-6c3e-4710-875b-cff5b14e5c95",
              text = "Add more information",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
          ),
        ),
      ),
    ),
    Task(
      code = "make-a-risk-management-decision",
      name = "Make a risk management decision",
      sections = listOf(
        Section(
          questions = listOf(
            Question(
              code = "can-offender-be-managed-safely",
              text = "Can {offenderForename} be managed safely in the community if they are released to the proposed address or CAS area?",
              input = Input(
                type = InputType.RADIO,
                options = listOf(
                  Option("Yes"),
                  Option("No"),
                ),
              ),
            ),
            Question(
              code = "information-to-support-decision",
              text = "Add information to support your decision",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
            Question(
              code = "any-risk-management-planning-actions-needed",
              text = "Are any risk management planning actions needed prior to release before the address or CAS area can be suitable?",
              input = Input(
                type = InputType.TEXT,
              ),
            ),
          ),
        ),
      ),
    ),
  ),
)
