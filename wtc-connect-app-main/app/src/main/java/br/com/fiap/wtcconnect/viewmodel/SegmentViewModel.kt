package br.com.fiap.wtcconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.data.repository.SegmentRepository
import br.com.fiap.wtcconnect.network.SegmentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SegmentUiState(
    val isLoading: Boolean = false,
    val segments: List<SegmentDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SegmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SegmentRepository
    private val _uiState = MutableStateFlow(SegmentUiState())
    val uiState: StateFlow<SegmentUiState> = _uiState.asStateFlow()

    init {
        AppContainer.init(application)
        repository = AppContainer.provideSegmentRepository()
        loadSegments()
    }

    fun loadSegments() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getSegments()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, segments = list)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar segmentos"
                    )
                }
        }
    }

    fun createSegment(name: String, description: String = "") {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nome do segmento é obrigatório")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.createSegment(name, description)
                .onSuccess { segment ->
                    val updated = _uiState.value.segments + segment
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        segments = updated,
                        successMessage = "Segmento criado com sucesso"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao criar segmento"
                    )
                }
        }
    }

    fun deleteSegment(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.deleteSegment(id)
                .onSuccess {
                    val updated = _uiState.value.segments.filterNot { it.id == id }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        segments = updated,
                        successMessage = "Segmento removido"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao remover segmento"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
