package com.example

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class StarWarsControllerTest : IntegrationTestBase() {

    @Test
    fun `test get people endpoint`() = withTestApp {
        val response = client.get("/api/people")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("\"count\":"), "Response should contain count field")
        assertTrue(responseText.contains("\"results\":"), "Response should contain results field")
    }

    @Test
    fun `test get person by id endpoint`() = withTestApp {
        val response = client.get("/api/people/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("\"name\":"), "Response should contain name field")
        assertTrue(
            responseText.contains("\"birth_year\":") || responseText.contains("\"birthYear\":"),
            "Response should contain birth year field",
        )
    }

    @Test
    fun `test get planets endpoint`() = withTestApp {
        val response = client.get("/api/planets")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("\"count\":"), "Response should contain count field")
        assertTrue(responseText.contains("\"results\":"), "Response should contain results field")
    }

    @Test
    fun `test get planet by id endpoint`() = withTestApp {
        val response = client.get("/api/planets/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("\"name\":"), "Response should contain name field")
        assertTrue(responseText.contains("\"climate\":"), "Response should contain climate field")
    }
}
