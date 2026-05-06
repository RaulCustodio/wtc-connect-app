package br.com.fiap.wtcconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.data.repository.CampaignRepository
import br.com.fiap.wtcconnect.network.CampaignDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CampaignUiState(
    val isLoading: Boolean = false,
    val campaigns: List<CampaignDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CampaignViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampaignRepository
    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    init {
        AppContainer.init(application)
        repository = AppContainer.provideCampaignRepository()
        loadCampaigns()
    }

    fun loadCampaigns() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getCampaigns()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, campaigns = list)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar campanhas"
                    )
                }
        }
    }

    fun createCampaign(name: String, content: String, targetCustomerIds: List<String>) {
        if (name.isBlank() || content.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nome e conteúdo são obrigatórios")
            return
        }
        if (targetCustomerIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Selecione ao menos um cliente")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.createCampaign(name, content, targetCustomerIds)
                .onSuccess { response ->
                    val updated = _uiState.value.campaigns + response.campaign
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        campaigns = updated,
                        successMessage = "Campanha criada! ${response.messagesSent} mensagem(ns) enviada(s)"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao criar campanha"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
