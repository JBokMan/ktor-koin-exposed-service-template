plugins {
  alias(libs.plugins.kotlin.jvm)
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
      title = "Ktor Server Title"
      description = "Ktor Server Description"
      version = "1.0"
      contact {
        name = "Inspektor"
        url = "https://github.com/tabilzad/ktor-docs-plugin"
      }
    }
  }

  pluginOptions {
    format = "yaml" // or json
  }
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories { mavenCentral() }

dependencies {
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.swagger)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.exposed.core)
  implementation(libs.exposed.jdbc)
  implementation(libs.h2)
  implementation(libs.ktor.server.netty)
  implementation(libs.koin.ktor)
  implementation(libs.ktor.server.config.yaml)
  implementation(libs.ktor.server.openapi)
  implementation(libs.kotlin.logging.jvm)
  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.kotlin.test.junit)
}
