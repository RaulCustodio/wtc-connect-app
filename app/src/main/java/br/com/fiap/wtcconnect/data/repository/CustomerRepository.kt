package br.com.fiap.wtcconnect.data.repository

import br.com.fiap.wtcconnect.network.CreateCustomerRequest
import br.com.fiap.wtcconnect.network.CustomerApi
import br.com.fiap.wtcconnect.network.CustomerDto

class CustomerRepository(
    private val customerApi: CustomerApi
) {
    suspend fun getCustomers(): List<CustomerDto> {
        return customerApi.getCustomers()
    }

    suspend fun createCustomer(
        userId: String,
        name: String,
        phone: String,
        address: String,
        segmentId: String?
    ): CustomerDto {
        return customerApi.createCustomer(
            CreateCustomerRequest(
                userId = userId,
                segmentId = segmentId,
                name = name,
                phone = phone,
                address = address
            )
        )
    }
}
