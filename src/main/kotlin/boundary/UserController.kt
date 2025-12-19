package com.example.boundary

import com.example.repository.ExposedUser
import com.example.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.koin.core.annotation.Single

@Single
class UserController(private val userRepository: UserRepository) {

    private val log = KotlinLogging.logger { this::class::simpleName }

    fun registerUserRoutes(application: Application) {
        application.routing {
            post(
                "/users",
                {
                    summary = "Create new user"
                    description = "Create a new user with the provided name and age"
                    request { body<ExposedUser>() }
                    response {
                        code(HttpStatusCode.Created) {
                            description =
                                "The User was created successfully, and the ID of the new user is returned"
                            body<Int>()
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "The request was malformed"
                        }
                    }
                },
            ) {
                val user = call.receive<ExposedUser>()
                log.debug { "Creating new user $user" }

                val id =
                    try {
                        userRepository.createNewUser(user)
                    } catch (e: Exception) {
                        log.error(e) { "Failed to create user" }
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                call.respond(HttpStatusCode.Created, id)
            }

            get(
                "/users/{id}",
                {
                    summary = "Get user by ID"
                    description = "Get the user with the provided ID"
                    request { pathParameter<Int>("id") { description = "The user ID" } }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "The user was found and returned"
                            body<ExposedUser>()
                        }
                        code(HttpStatusCode.NotFound) { description = "The user was not found" }
                    }
                },
            ) {
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

            get(
                "/users",
                {
                    summary = "Get all users"
                    description = "Get all users in the system."
                    response {
                        code(HttpStatusCode.OK) {
                            description = "All users were found and returned"
                            body<List<ExposedUser>>()
                        }
                    }
                },
            ) {
                log.debug { "Getting all users" }

                val users = userRepository.getAllUsers()

                call.respond(HttpStatusCode.OK, users)
            }

            put(
                "/users/{id}",
                {
                    summary = "Update user by ID"
                    description = "Update the user with the provided ID"
                    request {
                        pathParameter<Int>("id") { description = "The user ID" }
                        body<ExposedUser>()
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "The user was updated successfully"
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "The request was malformed"
                        }
                    }
                },
            ) {
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

            delete(
                "/users/{id}",
                {
                    summary = "Delete user by ID"
                    description = "Delete the user with the provided ID"
                    request { pathParameter<Int>("id") { description = "The user ID" } }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "The user was deleted successfully"
                        }
                        code(HttpStatusCode.NotFound) { description = "The user was not found" }
                    }
                },
            ) {
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
}
