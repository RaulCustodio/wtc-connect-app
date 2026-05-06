package br.com.fiap.wtcconnect.data.repository

import br.com.fiap.wtcconnect.network.CreateCustomerRequest
import br.com.fiap.wtcconnect.network.CustomerApi
import br.com.fiap.wtcconnect.network.CustomerDto
import br.com.fiap.wtcconnect.network.UpdateCustomerRequest

class CustomerRepository(private val api: CustomerApi) {

    suspend fun createCustomer(
        userId: String,
        name: String,
        email: String,
        phone: String,
        segmentId: String? = null
    ): Result<CustomerDto> = runCatching {
        api.createCustomer(CreateCustomerRequest(userId, name, email, phone, segmentId))
    }

    suspend fun getCustomerById(id: String): Result<CustomerDto> = runCatching {
        api.getCustomerById(id)
    }

    suspend fun getCustomerByUserId(userId: String): Result<CustomerDto> = runCatching {
        api.getCustomerByUserId(userId)
    }

    suspend fun getCustomerByEmail(email: String): Result<CustomerDto> = runCatching {
        api.getCustomerByEmail(email)
    }

    suspend fun getCustomersBySegment(segmentId: String): Result<List<CustomerDto>> = runCatching {
        api.getCustomersBySegment(segmentId)
    }

    suspend fun updateCustomer(
        id: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        segmentId: String? = null
    ): Result<CustomerDto> = runCatching {
        api.updateCustomer(UpdateCustomerRequest(id, name, email, phone, segmentId))
    }

    suspend fun deleteCustomer(id: String): Result<CustomerDto> = runCatching {
        api.deleteCustomer(id)
    }
}
