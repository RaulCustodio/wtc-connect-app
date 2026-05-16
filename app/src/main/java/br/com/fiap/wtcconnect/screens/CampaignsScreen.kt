package br.com.fiap.wtcconnect.screens.campaigns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.network.CampaignDto
import br.com.fiap.wtcconnect.network.CustomerDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen() {
    val campaignRepository = remember { AppContainer.provideCampaignRepository() }
    val customerRepository = remember { AppContainer.provideCustomerRepository() }
    val scope = rememberCoroutineScope()

    val campaigns = remember { mutableStateListOf<CampaignDto>() }
    val customers = remember { mutableStateListOf<CustomerDto>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNewCampaignDialog by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                val loadedCampaigns = campaignRepository.getCampaigns()
                val loadedCustomers = customerRepository.getCustomers()
                campaigns.clear()
                campaigns.addAll(loadedCampaigns)
                customers.clear()
                customers.addAll(loadedCustomers)
            }.onFailure { ex ->
                ex.printStackTrace()
                errorMessage = ex.message ?: "Erro ao carregar campanhas"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campanhas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewCampaignDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova campanha")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
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
                        Button(onClick = { loadData() }) {
                            Text("Tentar novamente")
                        }
                    }
                }

                campaigns.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Nenhuma campanha criada")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showNewCampaignDialog = true }) {
                            Text("Criar campanha")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(campaigns, key = { it.id ?: it.name }) { campaign ->
                            CampaignCard(campaign = campaign)
                        }
                    }
                }
            }
        }
    }

    if (showNewCampaignDialog) {
        NewCampaignDialog(
            customers = customers,
            onDismiss = { showNewCampaignDialog = false },
            onSave = { name, content, customerIds ->
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    runCatching {
                        campaignRepository.createCampaign(
                            name = name,
                            content = content,
                            targetCustomerIds = customerIds
                        )
                    }.onSuccess {
                        showNewCampaignDialog = false
                        loadData()
                    }.onFailure {
                        errorMessage = it.message ?: "Erro ao criar campanha"
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Composable
private fun CampaignCard(campaign: CampaignDto) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = campaign.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(campaign.content, color = Color.Gray)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                Text(
                    text = campaign.createdAt.take(10).ifBlank { "Sem data" },
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Destinatarios: ${campaign.targetCustomerIds.size}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun NewCampaignDialog(
    customers: List<CustomerDto>,
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val selectedCustomerIds = remember { mutableStateListOf<String>() }
    val canSave = name.isNotBlank() && content.isNotBlank() && selectedCustomerIds.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova campanha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Mensagem") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Clientes", style = MaterialTheme.typography.labelMedium)
                if (customers.isEmpty()) {
                    Text("Cadastre clientes antes de criar uma campanha.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(customers, key = { it.id ?: it.userId }) { customer ->
                            val customerId = customer.id.orEmpty()
                            val checked = customerId in selectedCustomerIds
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedCustomerIds.add(customerId)
                                        } else {
                                            selectedCustomerIds.remove(customerId)
                                        }
                                    },
                                    enabled = customerId.isNotBlank()
                                )
                                Column {
                                    Text(customer.name)
                                    Text(customer.id.orEmpty(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        name.trim(),
                        content.trim(),
                        selectedCustomerIds.toList()
                    )
                }
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
