<a name="phase-m3"></a>
# 📌 MOBILE PHASE M3: BACKEND & API INTEGRATION (Data Layer Engineer)

> **Rule:** Never perform network operations on the Main thread. Always use Coroutines with `Dispatchers.IO` and handle exceptions gracefully using a wrapper class like `Result<T>`.

---

### Prompt M3.1: Retrofit & OkHttp Setup

```text
You are a Data Layer Engineer. Set up the Retrofit networking client for [AppName].

Requirements:
- Use OkHttp for logging and interceptors.
- Use `kotlinx.serialization` for JSON parsing.
- Provide a Dagger Hilt module to inject the `Retrofit` instance and API interfaces.
- Include an Auth Interceptor that automatically attaches a Bearer token.

Required Output Format: Provide complete code for:

1. Network Module `di/NetworkModule.kt`:
```kotlin
package com.example.app.di

import com.example.app.data.remote.AuthInterceptor
import com.example.app.data.remote.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Use NONE in production
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

2. Auth Interceptor `data/remote/AuthInterceptor.kt`:
```kotlin
package com.example.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider // Your local secure storage wrapper
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        
        // Don't add token to auth endpoints
        if (!chain.request().url.encodedPath.contains("auth/login")) {
            tokenProvider.getToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
```

3. API Service Interface `data/remote/ApiService.kt`:
```kotlin
package com.example.app.data.remote

import com.example.app.data.remote.dto.ItemDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("items")
    suspend fun getItems(): Response<List<ItemDto>>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): Response<ItemDto>
}
```

⚠️ Common Pitfalls:
- Pitfall: Parsing dates incorrectly because `kotlinx.serialization` doesn't handle `java.util.Date` automatically.
- Solution: Use primitive types (Long for timestamp or String for ISO8601) in DTOs, or write a custom `KSerializer`.
```

---

### Prompt M3.2: Repository Pattern & Result Wrapper

```text
You are an Android Architect. Implement the Repository Pattern to abstract the Retrofit API, using a standard `Result` wrapper for error handling.

Requirements:
- Map DTOs (Data Transfer Objects) to Domain Models.
- Use Kotlin's built-in `Result<T>` or a custom sealed class (e.g., `Resource<T>`) for representing Success/Error.
- Ensure network calls run on `Dispatchers.IO`.

Required Output Format: Provide complete code for:

1. Resource Wrapper `domain/util/Resource.kt`:
```kotlin
package com.example.app.domain.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

2. Repository Implementation `data/repository/ItemRepositoryImpl.kt`:
```kotlin
package com.example.app.data.repository

import com.example.app.data.remote.ApiService
import com.example.app.domain.model.Item
import com.example.app.domain.repository.ItemRepository
import com.example.app.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ItemRepository {

    override suspend fun getItems(): Resource<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getItems()
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                Resource.Success(body.map { it.toDomainModel() }) // Map DTO to Model
            } else {
                Resource.Error(response.message() ?: "An unknown error occurred")
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.code()}")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }
}
```

3. DTO Mapping `data/remote/dto/ItemDto.kt`:
```kotlin
package com.example.app.data.remote.dto

import com.example.app.domain.model.Item
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String,
    val name: String,
    val description: String? = null
) {
    fun toDomainModel(): Item {
        return Item(
            id = id,
            title = name,
            subtitle = description ?: ""
        )
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Exposing Retrofit response objects (`Response<T>`) directly to the ViewModel.
- Solution: The Repository must map network objects to Domain models and wrap them in a `Resource` class, isolating the UI from network implementation details.
```

---

✅ **Verification Checklist:**
- [ ] Network Module compiles and Hilt provides `Retrofit` and `ApiService`.
- [ ] AuthInterceptor correctly attaches headers.
- [ ] Repository functions are `suspend` and explicitly use `Dispatchers.IO`.
- [ ] Exceptions (like `IOException` for no internet) are caught and mapped to `Resource.Error`.

---

📎 **Related Phases:**
- Prerequisites: [Phase M1: Project Structure](./MOBILE_PHASE_1_PROJECT_STRUCTURE_CONFIGURATION_Full_Stack_Mobile.md)
- Proceeds to: [Phase M4: Database & Offline Storage](./MOBILE_PHASE_4_DATABASE_OFFLINE_STORAGE_Mobile_Architect.md)
