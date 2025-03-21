plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.tabilzad.inspektor)
}

group = "com.example"

version = "0.0.1"

swagger {
  documentation {
    generateRequestSchemas = true
    hideTransientFields = true
    hidePrivateAndInternalFields = true
    deriveFieldRequirementFromTypeNullability = true
    info {
      title = "Service Name"
      description = "Service Description"
      version = "1.0"
    }
  }

  pluginOptions {
    format = "yaml"
  }
}

application {
  mainClass.set("com.example.Application")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ksp {
  arg("KOIN_DEFAULT_MODULE", "true")
}

repositories { mavenCentral() }

dependencies {
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.swagger)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.exposed.core)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  implementation(libs.postgresql)
  implementation(libs.hikaricp)
  implementation(libs.ktor.server.cio)
  implementation(libs.koin.ktor)
  implementation(libs.koin.annotations)
  implementation("io.ktor:ktor-client-okhttp-jvm:3.1.1")
  ksp(libs.koin.ksp.compiler)
  implementation(libs.ktor.server.config.yaml)
  implementation(libs.ktor.server.openapi)
  implementation(libs.kotlin.logging.jvm)
  implementation(libs.cohort.ktor)
  implementation(libs.cohort.logback)
  implementation(libs.cohort.hikari)
  implementation(libs.cohort.http)
  implementation(libs.ktor.okhttp)
  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.kotlin.test.junit)
}
