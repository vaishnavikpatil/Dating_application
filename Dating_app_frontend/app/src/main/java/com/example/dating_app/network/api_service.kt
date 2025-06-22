package com.example.dating_app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Request data classes
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class ConnectionRequestBody(val senderId: String, val receiverId: String)

// Response data classes
data class UserResponse(val _id: String, val name: String, val email: String)

data class ConnectionRequestResponse(
    val _id: String,
    val senderId: Sender,
    val receiverId: String,
    val status: String
)

data class Sender(
    val _id: String,
    val name: String
)


interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): UserResponse

    @GET("all") // Get all users
    suspend fun getAllUsers(): List<UserResponse>

    @GET("{userId}/connections") // Get user connections
    suspend fun getConnections(@Path("userId") userId: String): List<UserResponse>

    @POST("request") // Send connection request
    suspend fun sendRequest(@Body request: ConnectionRequestBody): ConnectionRequestResponse

    @GET("pending-requests/{userId}")
    suspend fun getPendingRequests(@Path("userId") userId: String): List<ConnectionRequestResponse>

    @POST("accept/{requestId}") // Accept connection request
    suspend fun acceptRequest(@Path("requestId") requestId: String): Map<String, String> // e.g. { "message": "Request accepted" }
}

object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://url:3000/api/") // Update your IP and port if needed
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
