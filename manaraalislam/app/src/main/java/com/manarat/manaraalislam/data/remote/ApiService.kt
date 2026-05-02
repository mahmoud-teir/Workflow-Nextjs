package com.manarat.manaraalislam.data.remote

import retrofit2.http.GET

interface ApiService {
    @GET("health")
    suspend fun checkHealth(): String

    companion object {
        const val BASE_URL = "https://api.manaraalislam.com/" // Placeholder
    }
}
