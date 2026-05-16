package br.com.fiap.wtcconnect.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.network.CustomerDto
import br.com.fiap.wtcconnect.ui.theme.AccentGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    onOpenChat: (CustomerDto) -> Unit
) {
    val customerRepository = remember { AppContainer.provideCustomerRepository() }
    val scope = rememberCoroutineScope()
    val customers = remember { mutableStateListOf<CustomerDto>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNewClientDialog by remember { mutableStateOf(false) }

    fun loadCustomers() {
        scope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                customerRepository.getCustomers()
            }.onSuccess { loaded ->
                customers.clear()
                customers.addAll(loaded)
            }.onFailure {
                errorMessage = it.message ?: "Erro ao carregar clientes"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCustomers()
    }

    val filtered = remember(searchQuery, customers.size) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            customers.toList()
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewClientDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo cliente")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar cliente") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { loadCustomers() }) {
                            Text("Tentar novamente")
                        }
                    }
                }

                filtered.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Nenhum cliente encontrado")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showNewClientDialog = true }) {
                            Text("Cadastrar cliente")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id ?: it.userId }) { customer ->
                            ClientListItem(
                                customer = customer,
                                onOpenChat = { onOpenChat(customer) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewClientDialog) {
        NewClientDialog(
            onDismiss = { showNewClientDialog = false },
            onSave = { userId, name, phone, address, segmentId ->
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    runCatching {
                        customerRepository.createCustomer(
                            userId = userId.ifBlank { "manual-${System.currentTimeMillis()}" },
                            name = name,
                            phone = phone,
                            address = address,
                            segmentId = segmentId.takeIf { it.isNotBlank() }
                        )
                    }.onSuccess {
                        showNewClientDialog = false
                        loadCustomers()
                    }.onFailure {
                        errorMessage = it.message ?: "Erro ao cadastrar cliente"
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Composable
private fun ClientListItem(
    customer: CustomerDto,
    onOpenChat: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenChat)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AccentGreen)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    customer.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    customer.phone.ifBlank { "Sem telefone" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    customer.address.ifBlank { "Sem endereco" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    customer.id.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOpenChat) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Abrir chat")
            }
        }
    }
}

@Composable
private fun NewClientDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var segmentId by remember { mutableStateOf("") }
    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereco") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("UserId vinculado (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = segmentId,
                    onValueChange = { segmentId = it },
                    label = { Text("SegmentId (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        userId.trim(),
                        name.trim(),
                        phone.trim(),
                        address.trim(),
                        segmentId.trim()
                    )
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
