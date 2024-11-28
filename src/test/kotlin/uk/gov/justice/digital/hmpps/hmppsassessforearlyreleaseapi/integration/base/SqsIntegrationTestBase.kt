package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base

import OffenderEventProcessingCompleteHandler
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.EventProcessingCompleteHandler
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.LocalStackContainer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SqsIntegrationTestBase : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @MockBean
  lateinit var done: EventProcessingCompleteHandler

  @MockBean
  lateinit var hmppsOffenderDone: OffenderEventProcessingCompleteHandler

  protected val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents")
      ?: throw MissingQueueException("HmppsTopic domainevents not found")
  }
  protected val domainEventsTopicSnsClient by lazy { domainEventsTopic.snsClient }
  protected val domainEventsTopicArn by lazy { domainEventsTopic.arn }

  protected val mergeOffenderQueue by lazy { hmppsQueueService.findByQueueId("domaineventsqueue") as HmppsQueue }

  protected val hmppsOffenderTopic by lazy {
    hmppsQueueService.findByTopicId("hmppsoffender")
      ?: throw MissingQueueException("HmppsTopic hmppsoffender not found")
  }
  protected val hmppsOffenderTopicSnsClient by lazy { hmppsOffenderTopic.snsClient }
  protected val hmppsOffenderTopicArn by lazy { hmppsOffenderTopic.arn }

  protected val updateComQueue by lazy { hmppsQueueService.findByQueueId("hmppsoffenderqueue") as HmppsQueue }

  @BeforeEach
  fun cleanQueue() {
    mergeOffenderQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(mergeOffenderQueue.queueUrl).build())
    await untilCallTo { mergeOffenderQueue.sqsClient.countMessagesOnQueue(mergeOffenderQueue.queueUrl).get() } matches { it == 0 }

    updateComQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(updateComQueue.queueUrl).build())
    await untilCallTo { updateComQueue.sqsClient.countMessagesOnQueue(updateComQueue.queueUrl).get() } matches { it == 0 }
  }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int? =
    mergeOffenderQueue.sqsClient.countMessagesOnQueue(mergeOffenderQueue.queueUrl).get()

  fun getNumberOfMessagesCurrentlyOnUpdateComQueue(): Int? =
    updateComQueue.sqsClient.countMessagesOnQueue(updateComQueue.queueUrl).get()

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @Suppress("unused")
    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
