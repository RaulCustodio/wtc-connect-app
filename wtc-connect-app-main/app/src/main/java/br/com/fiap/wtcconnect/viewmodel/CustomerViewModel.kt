package br.com.fiap.wtcconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.data.repository.CustomerRepository
import br.com.fiap.wtcconnect.network.CustomerDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerUiState(
    val isLoading: Boolean = false,
    val customers: List<CustomerDto> = emptyList(),
    val selectedCustomer: CustomerDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CustomerRepository
    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        AppContainer.init(application)
        repository = AppContainer.provideCustomerRepository()
    }

    fun getCustomerById(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getCustomerById(id)
                .onSuccess { customer ->
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedCustomer = customer)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao buscar cliente"
                    )
                }
        }
    }

    fun getCustomerByUserId(userId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getCustomerByUserId(userId)
                .onSuccess { customer ->
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedCustomer = customer)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao buscar cliente"
                    )
                }
        }
    }

    fun getCustomersBySegment(segmentId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getCustomersBySegment(segmentId)
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, customers = list)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao buscar clientes"
                    )
                }
        }
    }

    fun createCustomer(userId: String, name: String, email: String, phone: String, segmentId: String? = null) {
        if (name.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nome e e-mail são obrigatórios")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.createCustomer(userId, name, email, phone, segmentId)
                .onSuccess { customer ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedCustomer = customer,
                        successMessage = "Cliente criado com sucesso"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao criar cliente"
                    )
                }
        }
    }

    fun updateCustomer(id: String, name: String? = null, email: String? = null, phone: String? = null, segmentId: String? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.updateCustomer(id, name, email, phone, segmentId)
                .onSuccess { customer ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedCustomer = customer,
                        successMessage = "Cliente atualizado com sucesso"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao atualizar cliente"
                    )
                }
        }
    }

    fun deleteCustomer(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.deleteCustomer(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedCustomer = null,
                        successMessage = "Cliente removido com sucesso"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao remover cliente"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
