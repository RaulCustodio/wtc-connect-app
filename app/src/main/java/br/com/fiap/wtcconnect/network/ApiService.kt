package br.com.fiap.wtcconnect.network

import br.com.fiap.wtcconnect.data.MessageStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val role: String
)

data class SendMessageRequest(
    val customerId: String,
    val content: String,
    val campaignId: String? = null
)

data class UpdateMessageStatusRequest(
    val status: MessageStatus
)

data class MessageDto(
    val id: String? = null,
    val customerId: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val content: String = "",
    val status: MessageStatus = MessageStatus.Sent,
    val campaignId: String? = null,
    val createdAt: String = "",
    val deliveredAt: String? = null,
    val readAt: String? = null
)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}

interface MessageApi {
    @GET("inbox/{customerId}")
    suspend fun getInbox(@Path("customerId") customerId: String): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): MessageDto

    @PATCH("messages/{id}/status")
    suspend fun updateMessageStatus(
        @Path("id") id: String,
        @Body request: UpdateMessageStatusRequest
    ): MessageDto
}
