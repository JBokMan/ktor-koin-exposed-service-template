package com.example.boundary

import com.example.repository.ExposedUser
import com.example.repository.UserRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
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
    val log by inject<KLogger>()

    routing {
        @KtorResponds(
            mapping = [
                ResponseEntry(
                    status = "201",
                    type = Int::class,
                    description = "The User was created successfully, and the ID of the new user is returned"
                ),
                ResponseEntry(
                    status = "400",
                    type = Unit::class,
                    description = "The request was malformed"
                )
            ]
        )
        @KtorDescription(
            summary = "Create new user",
            description = "Create a new user with the provided name and age"
        )
        post("/users") {
            val user = call.receive<ExposedUser>()
            log.debug { "Creating new user $user" }

            val id = try {
                userRepository.createNewUser(user)
            } catch (e: Exception) {
                log.error(e) { "Failed to create user" }
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            call.respond(HttpStatusCode.Created, id)
        }

        @KtorResponds(
            mapping = [
                ResponseEntry(
                    status = "200",
                    type = ExposedUser::class,
                    description = "The user was found and returned"
                ),
                ResponseEntry(status = "404", type = Unit::class, description = "The user was not found")
            ]
        )
        @KtorDescription(
            summary = "Get user by ID",
            description = "Get the user with the provided ID"
        )
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt()

            log.debug { "Getting user with ID $id" }

            if (id == null) {
                log.error { "Invalid ID provided ${call.parameters["id"]}" }
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

        @KtorResponds(
            mapping = [
                ResponseEntry(
                    status = "200",
                    type = ExposedUser::class,
                    description = "All users were found and returned",
                    isCollection = true
                ),
            ]
        )
        @KtorDescription(
            summary = "Get all users",
            description = "Get all users in the system."
        )
        get("/users") {
            log.debug { "Getting all users" }

            val users = userRepository.getAllUsers()

            call.respond(HttpStatusCode.OK, users)
        }

        @KtorResponds(
            mapping = [
                ResponseEntry(status = "200", type = Unit::class, description = "The user was updated successfully"),
                ResponseEntry(status = "400", type = Unit::class, description = "The request was malformed")
            ]
        )
        @KtorDescription(
            summary = "Update user by ID",
            description = "Update the user with the provided ID"
        )
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt()

            log.debug { "Updating user with ID $id" }

            if (id == null) {
                log.error { "Invalid ID provided ${call.parameters["id"]}" }
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val user = call.receive<ExposedUser>()
            userRepository.updateUser(id, user)

            call.respond(HttpStatusCode.OK)
        }

        @KtorResponds(
            mapping = [
                ResponseEntry(status = "200", type = Unit::class, description = "The user was deleted successfully"),
                ResponseEntry(status = "404", type = Unit::class, description = "The user was not found")
            ]
        )
        @KtorDescription(
            summary = "Delete user by ID",
            description = "Delete the user with the provided ID"
        )
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt()

            log.debug { "Deleting user with ID $id" }

            if (id == null) {
                log.error { "Invalid ID provided ${call.parameters["id"]}" }
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            userRepository.deleteUser(id)

            call.respond(HttpStatusCode.OK)
        }
    }
}
