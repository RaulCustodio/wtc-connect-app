package br.com.fiap.wtcconnect.data.repository

import br.com.fiap.wtcconnect.network.CampaignApi
import br.com.fiap.wtcconnect.network.CampaignDto
import br.com.fiap.wtcconnect.network.CampaignResponse
import br.com.fiap.wtcconnect.network.CreateCampaignRequest

class CampaignRepository(private val api: CampaignApi) {

    suspend fun getCampaigns(): Result<List<CampaignDto>> = runCatching {
        api.getCampaigns()
    }

    suspend fun createCampaign(
        name: String,
        content: String,
        targetCustomerIds: List<String>
    ): Result<CampaignResponse> = runCatching {
        api.createCampaign(CreateCampaignRequest(name, content, targetCustomerIds))
    }
}
