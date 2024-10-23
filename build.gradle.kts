plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.6"
  kotlin("plugin.spring") version "2.0.10"
  kotlin("plugin.jpa") version "2.0.10"
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

val integrationTest = task<Test>("integrationTest") {
  description = "Integration tests"
  group = "verification"
  shouldRunAfter("test")
}

tasks.register<Copy>("installLocalGitHook") {
  from(File(rootProject.rootDir, ".scripts/pre-commit"))
  into(File(rootProject.rootDir, ".git/hooks"))
  filePermissions { unix(755) }
}

tasks.named<Test>("integrationTest") {
  useJUnitPlatform()
  filter {
    includeTestsMatching("*.integration.*")
  }
}

tasks.named<Test>("test") {
  filter {
    excludeTestsMatching("*.integration.*")
  }
}

detekt {
  source.setFrom("$projectDir/src/main")
  buildUponDefaultConfig = true // preconfigure defaults
  allRules = false // activate all available (even unstable) rules.
  config.setFrom("$projectDir/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
  baseline = file("$projectDir/detekt-baseline.xml") // a way of suppressing issues before introducing detekt
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.7")
  implementation("org.springframework.security:spring-security-config:6.3.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("io.opentelemetry:opentelemetry-api:1.40.0")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.6.0")
  implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.2")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.3")

  // SQS/SNS dependencies
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.0.0")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.4")
  testImplementation("org.wiremock:wiremock-standalone:3.9.1")
  testImplementation("com.h2database:h2")
  testImplementation("org.testcontainers:postgresql:1.20.0")
  testImplementation("org.testcontainers:localstack:1.19.8")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.22") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
  withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
      html.required.set(true) // observe findings in your browser with structure and code snippets
    }
  }
  named("check").configure {
    this.setDependsOn(
      this.dependsOn.filterNot {
        it is TaskProvider<*> && it.name == "detekt"
      },
    )
  }
}

configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
    }
  }
}

allOpen {
  annotation("jakarta.persistence.Entity")
}
