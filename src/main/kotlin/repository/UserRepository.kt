package com.example.repository

import org.koin.java.KoinJavaComponent.inject

class UserRepository() {
    val userService by inject<UserService>(UserService::class.java)

    suspend fun createNewUser(exposedUser: ExposedUser): Int {
        return userService.create(exposedUser)
    }

    suspend fun getUserById(id: Int): ExposedUser? {
        return userService.read(id)
    }

    suspend fun getAllUsers(): List<ExposedUser> {
        return userService.readAll()
    }

    suspend fun updateUser(id: Int, exposedUser: ExposedUser) {
        return userService.update(id, exposedUser)
    }

    suspend fun deleteUser(id: Int) {
        return userService.delete(id)
    }
}