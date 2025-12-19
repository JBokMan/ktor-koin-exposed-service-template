plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.tabilzad.inspektor)
    alias(libs.plugins.ktfmt)
}

group = "com.example"

version = libs.versions.project.version.get()

swagger {
    documentation {
        generateRequestSchemas = true
        hideTransientFields = true
        hidePrivateAndInternalFields = true
        deriveFieldRequirementFromTypeNullability = true
        info {
            title = "Service Name"
            description = "Service Description"
            version = libs.versions.swagger.api.version.get()
        }
    }

    pluginOptions { format = "yaml" }
}

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ksp { arg("KOIN_DEFAULT_MODULE", "true") }

ktfmt { kotlinLangStyle() }

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
    ksp(libs.koin.ksp.compiler)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.openapi)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.cohort.ktor)
    implementation(libs.cohort.logback)
    implementation(libs.cohort.hikari)
    implementation(libs.cohort.http)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.okhttp.jvm)
    implementation(libs.kafka.clients)
    implementation(libs.reactor.kafka)
    implementation(libs.retrofit)
    implementation(libs.okio)
    implementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.testcontainers.junit.jupiter)
}

tasks.withType<Test> { useJUnitPlatform() }

val installLocalPostCommitGitHook =
    tasks.register<Copy>("installLocalPreCommitGitHook") {
        from(rootProject.file("scripts/post-commit"))
        into(rootProject.file(".git/hooks"))

        filePermissions {
            user.read = true
            user.write = true
            user.execute = true

            group.read = true
            group.write = false
            group.execute = true

            other.read = true
            other.write = false
            other.execute = true
        }
    }

tasks.named("processResources") {
    dependsOn(installLocalPostCommitGitHook)
    dependsOn("installLocalPreCommitGitHook") // Assuming this task is also correctly registered
}

tasks.named("ktfmtCheckMain") { enabled = false }

tasks.named("ktfmtCheckTest") { enabled = false }

tasks.named("ktfmtCheckScripts") { enabled = false }
