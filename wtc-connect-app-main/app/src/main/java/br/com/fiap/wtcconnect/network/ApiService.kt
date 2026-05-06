package br.com.fiap.wtcconnect.network

import br.com.fiap.wtcconnect.data.MessageStatus
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// ─── Auth ──────────────────────────────────────────────────────────────────

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

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}

// ─── Messages ──────────────────────────────────────────────────────────────

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

// ─── Customer (CRM) ────────────────────────────────────────────────────────

data class CustomerDto(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val segmentId: String? = null
)

data class CreateCustomerRequest(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val segmentId: String? = null
)

data class UpdateCustomerRequest(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val segmentId: String? = null
)

interface CustomerApi {
    @POST("customer")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CustomerDto

    @GET("customer/{id}")
    suspend fun getCustomerById(@Path("id") id: String): CustomerDto

    @GET("customer/user/id/{userId}")
    suspend fun getCustomerByUserId(@Path("userId") userId: String): CustomerDto

    @GET("customer/user/email/{email}")
    suspend fun getCustomerByEmail(@Path("email") email: String): CustomerDto

    @GET("customer/segment/{segmentId}")
    suspend fun getCustomersBySegment(@Path("segmentId") segmentId: String): List<CustomerDto>

    @PATCH("customer")
    suspend fun updateCustomer(@Body request: UpdateCustomerRequest): CustomerDto

    @DELETE("customer/{id}")
    suspend fun deleteCustomer(@Path("id") id: String): CustomerDto
}

// ─── Campaigns ─────────────────────────────────────────────────────────────

data class CampaignDto(
    val id: String = "",
    val name: String = "",
    val content: String = "",
    val targetCustomerIds: List<String> = emptyList(),
    val createdBy: String = "",
    val status: String = "",
    val createdAt: String = "",
    val sentAt: String? = null
)

data class CreateCampaignRequest(
    val name: String,
    val content: String,
    val targetCustomerIds: List<String>
)

data class CampaignResponse(
    val campaign: CampaignDto,
    val messagesSent: Int
)

interface CampaignApi {
    @GET("campaigns")
    suspend fun getCampaigns(): List<CampaignDto>

    @POST("campaigns")
    suspend fun createCampaign(@Body request: CreateCampaignRequest): CampaignResponse
}

// ─── Segments ──────────────────────────────────────────────────────────────

data class SegmentDto(
    val id: String = "",
    val name: String = "",
    val description: String = ""
)

data class CreateSegmentRequest(
    val name: String,
    val description: String = ""
)

data class UpdateSegmentRequest(
    val id: String,
    val name: String? = null,
    val description: String? = null
)

interface SegmentApi {
    @GET("segment")
    suspend fun getSegments(): List<SegmentDto>

    @POST("segment")
    suspend fun createSegment(@Body request: CreateSegmentRequest): SegmentDto

    @GET("segment/id/{id}")
    suspend fun getSegmentById(@Path("id") id: String): SegmentDto

    @GET("segment/name/{name}")
    suspend fun getSegmentByName(@Path("name") name: String): SegmentDto

    @PATCH("segment")
    suspend fun updateSegment(@Body request: UpdateSegmentRequest): SegmentDto

    @DELETE("segment/{id}")
    suspend fun deleteSegment(@Path("id") id: String): SegmentDto
}
