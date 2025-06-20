package com.example

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class ApplicationTest : IntegrationTestBase() {

    @Test
    fun testRoot() = withTestApp {
        // Act
        val response = client.get("/swagger")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetEmptyUsers() = withTestApp {
        // Act
        val response = client.get("/users")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText())
    }
}
