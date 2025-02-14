package com.example.boundary

import com.example.repository.ExposedUser
import com.example.repository.UserRepository
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

@GenerateOpenApi
fun Application.userRoutes() {

    val userRepository by inject<UserRepository>()

    routing {
        post("/users") {
            val user = call.receive<ExposedUser>()

            val id = try {
                userRepository.createNewUser(user)
            } catch (e: Exception) {
                log.error("Failed to create user", e)
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            call.respond(HttpStatusCode.Created, id)
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                log.error("Invalid ID provided ${call.parameters["id"]}")
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val user = userRepository.getUserById(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                log.error("Invalid ID provided ${call.parameters["id"]}")
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val user = call.receive<ExposedUser>()
            userRepository.updateUser(id, user)

            call.respond(HttpStatusCode.OK)
        }

        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                log.error("Invalid ID provided ${call.parameters["id"]}")
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            userRepository.deleteUser(id)

            call.respond(HttpStatusCode.OK)
        }
    }
}
