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

data class CampaignDto(
    val id: String? = null,
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

data class CreateCampaignResponse(
    val campaign: CampaignDto,
    val messagesSent: Int
)

data class CustomerDto(
    val id: String? = null,
    val userId: String = "",
    val segmentId: String? = null,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val createdAt: String = "",
    val lastActionAt: String = ""
)

data class CreateCustomerRequest(
    val userId: String,
    val segmentId: String? = null,
    val name: String,
    val phone: String,
    val address: String
)

data class GroupDto(
    val id: String? = null,
    val name: String = "",
    val createdAt: String = ""
)

data class GroupMemberDto(
    val id: String? = null,
    val groupId: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String? = null,
    val addedAt: String = ""
)

data class AddGroupMemberRequest(
    val email: String
)

data class UserGroupResponse(
    val groupId: String? = null
)

data class SendGroupMessageRequest(
    val content: String
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

interface CampaignApi {
    @GET("campaigns")
    suspend fun getCampaigns(): List<CampaignDto>

    @POST("campaigns")
    suspend fun createCampaign(@Body request: CreateCampaignRequest): CreateCampaignResponse
}

interface CustomerApi {
    @GET("customer")
    suspend fun getCustomers(): List<CustomerDto>

    @POST("customer")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CustomerDto
}

interface GroupApi {
    @GET("groups")
    suspend fun getGroups(): List<GroupDto>

    @GET("groups/{groupId}/members")
    suspend fun getGroupMembers(@Path("groupId") groupId: String): List<GroupMemberDto>

    @POST("groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: String,
        @Body request: AddGroupMemberRequest
    ): GroupMemberDto

    @retrofit2.http.DELETE("groups/{groupId}/members/{userId}")
    suspend fun removeGroupMember(
        @Path("groupId") groupId: String,
        @Path("userId") userId: String
    ): GroupMemberDto

    @GET("groups/users/{userId}/group")
    suspend fun getUserGroup(@Path("userId") userId: String): UserGroupResponse

    @GET("groups/users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): GroupMemberDto

    @GET("groups/users/search")
    suspend fun searchUsers(
        @retrofit2.http.Query("query") query: String,
        @retrofit2.http.Query("groupId") groupId: String? = null
    ): List<GroupMemberDto>

    @GET("groups/{groupId}/messages")
    suspend fun getGroupMessages(@Path("groupId") groupId: String): List<MessageDto>

    @POST("groups/{groupId}/messages")
    suspend fun sendGroupMessage(
        @Path("groupId") groupId: String,
        @Body request: SendGroupMessageRequest
    ): MessageDto
}
