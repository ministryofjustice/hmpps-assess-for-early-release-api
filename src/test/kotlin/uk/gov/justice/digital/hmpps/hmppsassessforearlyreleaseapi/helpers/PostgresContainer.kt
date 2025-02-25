package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  private const val DB_NAME = "afer-test-db"
  const val DB_USERNAME = "afer"
  const val DB_PASSWORD = "dummy"
  private const val DB_DEFAULT_PORT = 5433
  const val DB_DEFAULT_URL = "jdbc:postgresql://localhost:$DB_DEFAULT_PORT/$DB_NAME?sslmode=prefer"

  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? = if (checkPostgresRunning().not()) {
    PostgreSQLContainer<Nothing>("postgres:16.3").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withDatabaseName(DB_NAME)
      withUsername(DB_USERNAME)
      withPassword(DB_PASSWORD)
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  } else {
    null
  }

  private fun checkPostgresRunning(): Boolean = try {
    val serverSocket = ServerSocket(DB_DEFAULT_PORT)
    serverSocket.localPort == 0
  } catch (e: IOException) {
    true
  }
}
