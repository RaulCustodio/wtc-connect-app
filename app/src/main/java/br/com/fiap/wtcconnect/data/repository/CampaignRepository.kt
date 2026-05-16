package br.com.fiap.wtcconnect.data.repository

import br.com.fiap.wtcconnect.network.CampaignApi
import br.com.fiap.wtcconnect.network.CampaignDto
import br.com.fiap.wtcconnect.network.CreateCampaignRequest

class CampaignRepository(
    private val campaignApi: CampaignApi
) {
    suspend fun getCampaigns(): List<CampaignDto> {
        return campaignApi.getCampaigns()
    }

    suspend fun createCampaign(
        name: String,
        content: String,
        targetCustomerIds: List<String>
    ): CampaignDto {
        val response = campaignApi.createCampaign(
            CreateCampaignRequest(
                name = name,
                content = content,
                targetCustomerIds = targetCustomerIds
            )
        )
        return response.campaign ?: CampaignDto(
            name = name,
            content = content,
            targetCustomerIds = targetCustomerIds
        )
    }
}
