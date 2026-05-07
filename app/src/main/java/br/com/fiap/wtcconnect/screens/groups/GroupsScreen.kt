package br.com.fiap.wtcconnect.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme
import br.com.fiap.wtcconnect.data.ChatRepository
import br.com.fiap.wtcconnect.data.User
import br.com.fiap.wtcconnect.data.Group
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import br.com.fiap.wtcconnect.viewmodel.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(navController: NavController, repository: ChatRepository, currentUserId: String?, currentUserType: UserType) {
    val groups by produceState(initialValue = emptyList<Group>(), key1 = repository) {
        try {
            repository.getGroups().collect { value = it }
        } catch (_: Exception) {
            value = emptyList()
        }
    }
    val scope = rememberCoroutineScope()

    // myGroupId como State derivado diretamente da Flow do repositório (seguro via produceState)
    val myGroupId by produceState<String?>(initialValue = null, key1 = currentUserId, key2 = repository) {
        if (currentUserId == null) {
            value = null
        } else {
            try {
                repository.getUserGroupId(currentUserId).collect { value = it }
            } catch (_: Exception) {
                value = null
            }
        }
    }

    // Determina o grupo efetivo: myGroupId (se existir) ou o grupo default 'g0'
    val defaultGroup = groups.find { it.id == "g0" } ?: groups.find { it.name == "WTC Connect" }
    val effectiveGroupId = myGroupId ?: defaultGroup?.id

    // membros do grupo efetivo (para clientes) — coleta reativa baseada em effectiveGroupId
    val groupMembers by produceState(initialValue = emptyList<User>(), key1 = effectiveGroupId) {
        if (effectiveGroupId == null) {
            value = emptyList()
        } else {
            try {
                repository.getGroupMembers(effectiveGroupId).collect { list -> value = list }
            } catch (_: Exception) {
                // evita crash: retorna lista vazia em caso de erro
                value = emptyList()
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var managingGroupId by remember { mutableStateOf<String?>(null) }
    var manageEmail by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    // membros do grupo sendo gerenciado (para operador)
    val managingGroupMembers by produceState(initialValue = emptyList<User>(), key1 = managingGroupId) {
        val mgId = managingGroupId
        if (mgId == null) {
            value = emptyList()
        } else {
            try {
                repository.getGroupMembers(mgId).collect { list -> value = list }
            } catch (_: Exception) {
                value = emptyList()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Grupos") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White))
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {

            if (currentUserType == UserType.OPERATOR) {
                // Loading: aguarda groups carregarem
                if (groups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Column
                }

                Text("Painel de Gestão (Operador)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // Lista todos os grupos com botão para gerenciar membros
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(groups) { group ->
                        Card(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(group.name, fontWeight = FontWeight.Medium)
                                }
                                Row {
                                    Button(onClick = {
                                        // navegar para chat de grupo
                                        navController.navigate("group_chat/${group.id}")
                                    }) { Text("Abrir chat do grupo") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        // abrir dialogo de gerenciamento: abre o diálogo abaixo
                                        managingGroupId = group.id
                                        manageEmail = ""
                                    }) { Text("Gerenciar") }
                                }
                            }
                        }
                    }
                }
            } else {
                // Cliente: mostrar apenas seu grupo efetivo (myGroupId ou default 'g0') e botão para abrir chat do grupo

                // Se não há grupos carregados ainda, mostra loading
                if (groups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Column
                }

                val myGroup = groups.find { it.id == effectiveGroupId }

                if (effectiveGroupId != null && myGroup != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(myGroup.name, fontWeight = FontWeight.Medium)
                                Button(onClick = { navController.navigate("group_chat/${myGroup.id}") }) { Text("Abrir chat do grupo") }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Membros", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Exibe somente membros que possuem e-mail (assumimos que isso indica usuário já cadastrado/logado)
                            val loggedMembers = groupMembers.filter { it.email != null }
                            if (loggedMembers.isEmpty()) {
                                Text("Nenhum membro já cadastrado encontrado no grupo", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            } else {
                                loggedMembers.forEach { user ->
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        // Avatar iniciais
                                        val initials = user.name.firstOrNull()?.toString() ?: user.email?.firstOrNull()?.toString() ?: "U"
                                        Box(modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0033A0)), contentAlignment = Alignment.Center) {
                                            Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(user.name, fontWeight = FontWeight.Medium)
                                            Text(user.email ?: "sem e-mail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    Text("Faça login para ver seu grupo")
                }
            }
        }
    }

    // Dialogo de gerenciamento (apenas para operadores)
    if (managingGroupId != null && currentUserType == UserType.OPERATOR) {
        val mg = groups.find { it.id == managingGroupId }
        AlertDialog(
            onDismissRequest = { managingGroupId = null },
            title = { Text(text = "Gerenciar grupo: ${mg?.name ?: "-"}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = manageEmail,
                        onValueChange = { manageEmail = it },
                        label = { Text("Adicionar usuário por e-mail") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Membros do grupo", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (managingGroupMembers.isEmpty()) {
                        Text("Nenhum membro", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        // lista com botão para remover
                        managingGroupMembers.forEach { u ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(u.name)
                                    Text(u.email ?: "sem e-mail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                IconButton(onClick = {
                                    // remover usuário do grupo com proteção contra NPE e exceções
                                    val mgId = managingGroupId
                                    if (mgId == null) {
                                        scope.launch { snackbarHostState.showSnackbar("Grupo inválido") }
                                        return@IconButton
                                    }
                                    scope.launch {
                                        try {
                                            isProcessing = true
                                            val result = repository.removeUserFromGroup(mgId, u.id)
                                            if (result.isSuccess) {
                                                snackbarHostState.showSnackbar("Usuário ${u.name} removido do grupo")
                                            } else {
                                                snackbarHostState.showSnackbar("Falha ao remover: ${result.exceptionOrNull()?.message ?: "erro"}")
                                            }
                                        } catch (ex: Exception) {
                                            snackbarHostState.showSnackbar("Erro ao remover: ${ex.message}")
                                        } finally {
                                            isProcessing = false
                                        }
                                    }
                                }, enabled = !isProcessing) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover")
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // adicionar por email com proteção e tratamento de exceção
                    val mgId = managingGroupId
                    if (mgId == null) {
                        scope.launch { snackbarHostState.showSnackbar("Grupo inválido") }
                        return@TextButton
                    }
                    scope.launch {
                        val email = manageEmail.trim()
                        if (email.isNotEmpty()) {
                            try {
                                isProcessing = true
                                val result = repository.addUserToGroupByEmail(mgId, email)
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar("Usuário adicionado ao grupo")
                                    manageEmail = ""
                                } else {
                                    snackbarHostState.showSnackbar("Falha: ${result.exceptionOrNull()?.message ?: "erro"}")
                                }
                            } catch (ex: Exception) {
                                snackbarHostState.showSnackbar("Erro ao adicionar: ${ex.message}")
                            } finally {
                                isProcessing = false
                            }
                        } else {
                            snackbarHostState.showSnackbar("Informe um e-mail válido")
                        }
                    }
                }, enabled = !isProcessing) {
                    if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { managingGroupId = null }) { Text("Fechar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupsScreenPreview() {
    WtcCrmTheme {
        // Preview sem repo
        //GroupsScreen(navController = rememberNavController(), repository = /* fake */)
    }
}
