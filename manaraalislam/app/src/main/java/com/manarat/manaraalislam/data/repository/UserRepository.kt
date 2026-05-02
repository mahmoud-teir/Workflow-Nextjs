package com.manarat.manaraalislam.data.repository

import com.manarat.manaraalislam.data.local.UserDao
import com.manarat.manaraalislam.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class UserRepository(private val userDao: UserDao) {

    val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()

    suspend fun loginAsGuest() {
        val randomId = Random.nextInt(1000, 9999).toString()
        val randomImageIndex = Random.nextInt(1, 5)
        val guestUser = UserEntity(
            id = "GUEST_$randomId",
            name = "Guest User $randomId",
            email = null,
            profileImageUrl = "placeholder_$randomImageIndex",
            isGuest = true
        )
        userDao.insertUser(guestUser)
    }

    suspend fun loginWithEmail(email: String, name: String) {
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            profileImageUrl = null,
            isGuest = false
        )
        userDao.insertUser(user)
    }

    suspend fun updateInterests(interests: List<String>) {
        // Since we only have one user in the DB at a time for MVP
        userDao.getCurrentUser().collect { user ->
            user?.let {
                userDao.updateUser(it.copy(interests = interests))
            }
        }
    }

    suspend fun logout() {
        userDao.clearAll()
    }
}
