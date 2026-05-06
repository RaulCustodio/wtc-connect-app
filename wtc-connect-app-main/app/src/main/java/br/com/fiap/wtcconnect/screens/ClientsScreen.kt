package br.com.fiap.wtcconnect.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcconnect.ui.theme.AccentGreen
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme
import br.com.fiap.wtcconnect.data.model.Client

val mockClients = listOf(
    Client(1, "Empresa Alpha", "Ativo", listOf("VIP", "Lead Quente"), 95),
    Client(2, "Soluções Beta", "Inativo", listOf("Ex-cliente"), 30),
    Client(3, "Inovações Gamma", "Em prospecção", listOf("Follow-up"), 70),
    Client(4, "Tech Delta", "Ativo", listOf("Contrato Anual"), 88)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    onOpenChat: (Client) -> Unit // 🔥 ADICIONADO
) {
    var searchQuery by remember { mutableStateOf("") }

    val clients = remember {
        mutableStateListOf<Client>().apply {
            addAll(mockClients)
        }
    }

    val allStatuses = listOf("Ativo", "Inativo", "Em prospecção")
    val allTags = remember(clients.size) { clients.flatMap { it.tags }.distinct() }

    var selectedStatuses by remember { mutableStateOf(setOf<String>()) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var minScore by remember { mutableStateOf(0f) }

    val notesByClient = remember { mutableStateMapOf<Int, SnapshotStateList<String>>() }
    var showNoteDialogFor by remember { mutableStateOf<Int?>(null) }
    var noteText by remember { mutableStateOf("") }

    var showNewClientDialog by remember { mutableStateOf(false) }
    var newClientName by remember { mutableStateOf("") }
    var newClientStatus by remember { mutableStateOf("Ativo") }
    var newClientTags by remember { mutableStateOf("") }
    var newClientScore by remember { mutableStateOf("0") }

    val filtered = remember(searchQuery, selectedStatuses, selectedTags, minScore, clients.size) {
        clients.filter { client ->
            val matchesQuery = client.name.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatuses.isEmpty() || client.status in selectedStatuses
            val matchesTags = selectedTags.isEmpty() || selectedTags.all { it in client.tags }
            val matchesScore = client.score >= minScore.toInt()
            matchesQuery && matchesStatus && matchesTags && matchesScore
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
                Icon(Icons.Default.Add, contentDescription = "Novo Cliente")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            // 🔎 BUSCA + FILTROS (sem alteração)
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar cliente...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Status", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allStatuses.forEach { status ->
                        val selected = status in selectedStatuses
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedStatuses = selectedStatuses.toMutableSet().apply {
                                    if (selected) remove(status) else add(status)
                                }
                            },
                            label = { Text(status) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tags", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag ->
                        val selected = tag in selectedTags
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedTags = selectedTags.toMutableSet().apply {
                                    if (selected) remove(tag) else add(tag)
                                }
                            },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Score mínimo: ${minScore.toInt()}", color = Color.Gray)

                Slider(
                    value = minScore,
                    onValueChange = { minScore = it },
                    valueRange = 0f..100f
                )
            }

            // 📋 LISTA DE CLIENTES
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { client ->
                    val noteCount = notesByClient[client.id]?.size ?: 0

                    ClientListItem(
                        client = client,
                        noteCount = noteCount,
                        onAddNote = {
                            showNoteDialogFor = client.id
                        },

                        // 🔥 AQUI ESTÁ A CORREÇÃO DO CHAT
                        onNewMessage = {
                            onOpenChat(client)
                        }
                    )
                }
            }
        }
    }

    if (showNoteDialogFor != null) {
        AlertDialog(
            onDismissRequest = {
                showNoteDialogFor = null
                noteText = ""
            },
            title = { Text("Nova anotação") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Digite uma anotação rápida...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = showNoteDialogFor
                    if (id != null && noteText.isNotBlank()) {
                        val list = notesByClient.getOrPut(id) { mutableStateListOf() }
                        list.add(noteText.trim())
                    }
                    noteText = ""
                    showNoteDialogFor = null
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNoteDialogFor = null
                    noteText = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showNewClientDialog) {
        AlertDialog(
            onDismissRequest = { showNewClientDialog = false },
            title = { Text("Novo Cliente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newClientName,
                        onValueChange = { newClientName = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newClientStatus,
                        onValueChange = { newClientStatus = it },
                        label = { Text("Status") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newClientTags,
                        onValueChange = { newClientTags = it },
                        label = { Text("Tags (separadas por vírgula)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newClientScore,
                        onValueChange = { newClientScore = it },
                        label = { Text("Score") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newClientName.isNotBlank()) {
                        val nextId = (clients.maxOfOrNull { it.id } ?: 0) + 1
                        clients.add(
                            Client(
                                id = nextId,
                                name = newClientName.trim(),
                                status = newClientStatus.trim().ifBlank { "Ativo" },
                                tags = newClientTags
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() },
                                score = newClientScore.toIntOrNull() ?: 0
                            )
                        )

                        newClientName = ""
                        newClientStatus = "Ativo"
                        newClientTags = ""
                        newClientScore = "0"
                        showNewClientDialog = false
                    }
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewClientDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ClientListItem(
    client: Client,
    noteCount: Int,
    onAddNote: () -> Unit,
    onNewMessage: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
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
                    .background(if (client.status == "Ativo") AccentGreen else Color.Gray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(client.name, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(client.status, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Score ${client.score}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Row {
                    client.tags.forEach { tag ->
                        Chip(label = tag)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            IconButton(onClick = onNewMessage) {
                Icon(Icons.Default.Message, contentDescription = "Nova Mensagem")
            }
            BadgedBox(
                badge = {
                    if (noteCount > 0) {
                        Badge { Text(noteCount.toString()) }
                    }
                }
            ) {
                IconButton(onClick = onAddNote) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Adicionar Anotação")
                }
            }
        }
    }
}

@Composable
fun Chip(label: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ClientsScreenPreview() {
    WtcCrmTheme {
        ClientsScreen(
            onOpenChat = {}
        )
    }
}