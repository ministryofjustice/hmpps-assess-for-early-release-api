plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.3"
  kotlin("plugin.spring") version "2.0.10"
  kotlin("plugin.jpa") version "2.0.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.4")
  implementation("org.springframework.security:spring-security-config:6.3.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.3")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.4")
  testImplementation("org.wiremock:wiremock-standalone:3.8.0")
  testImplementation("com.h2database:h2")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}

allOpen {
  annotation("jakarta.persistence.Entity")
}
