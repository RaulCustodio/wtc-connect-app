package br.com.fiap.wtcconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.wtcconnect.ui.theme.RoyalBlue
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme
import br.com.fiap.wtcconnect.ui.theme.White
import br.com.fiap.wtcconnect.data.ChatRepository
import br.com.fiap.wtcconnect.data.Conversation
import br.com.fiap.wtcconnect.data.FakeChatRepository
import br.com.fiap.wtcconnect.presentation.ConversationListViewModel
import br.com.fiap.wtcconnect.presentation.ConversationListViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(navController: NavController, repository: ChatRepository? = null, currentUserId: String? = null, currentUserType: br.com.fiap.wtcconnect.viewmodel.UserType = br.com.fiap.wtcconnect.viewmodel.UserType.CLIENT) {
    val repo = repository ?: FakeChatRepository(currentUserId = currentUserId ?: "me")
    val vm: ConversationListViewModel = viewModel(factory = ConversationListViewModelFactory(repo))
    val state by vm.uiState.collectAsState()
    // Computa a lista filtrada localmente a partir do state do ViewModel.
    // Isso garante que a UI reaja imediatamente às mudanças de `state.query`.
    // Primeiro aplica filtro por grupo (se for CLIENT), depois aplica a busca por nome/email
    // (não é necessário criar um MutableStateFlow aqui; o groupId é coletado abaixo)

    // Como repo.getUserGroupId retorna um Flow, coletemos o groupId atual do usuário
    var effectiveGroupId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentUserId) {
        currentUserId?.let { uid ->
            repo.getUserGroupId(uid).collect { gid ->
                effectiveGroupId = gid
            }
        }
    }

    // Colete membros do grupo atual (para clientes) para filtrar conversas 1:1
    val groupMemberIds by produceState(initialValue = emptyList<String>(), key1 = effectiveGroupId) {
        if (effectiveGroupId == null) {
            value = emptyList()
        } else {
            repo.getGroupMembers(effectiveGroupId!!).collect { list ->
                value = list.map { it.id }
            }
        }
    }

    val list = remember(state.conversations, state.query, effectiveGroupId, groupMemberIds, currentUserType) {
        val q = state.query.trim()
        val base = if (currentUserType == br.com.fiap.wtcconnect.viewmodel.UserType.CLIENT && effectiveGroupId != null && groupMemberIds.isNotEmpty()) {
            // inclui apenas conversas cujo peerUser esteja no mesmo grupo do usuário
            state.conversations.filter { convo -> groupMemberIds.contains(convo.peerUser.id) }
        } else {
            state.conversations
        }

        if (q.isEmpty()) base
        else base.filter { it.peerUser.name.contains(q, ignoreCase = true) || (it.peerUser.email?.contains(q, ignoreCase = true) ?: false) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalBlue,
                    titleContentColor = White
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(8.dp)) {

            // Campo de busca para filtrar conversas
            OutlinedTextField(
                value = state.query,
                onValueChange = { vm.onQueryChanged(it) },
                placeholder = { Text("Buscar contatos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    // Estado de erro com CTA para tentar novamente
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Erro: ${state.error}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { /* Re-tentar futuramente */ }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }
                list.isEmpty() -> {
                    // Estado vazio
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sem conversas ainda")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                // CTA mock: poderia abrir tela de criar conversa
                            }) {
                                Text("Criar nova conversa")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(list) { conversation ->
                            ConversationListItem(conversation = conversation, onClick = {
                                // Quando usuário toca, navega para ChatScreen com argumentos
                                vm.onSelectConversation(conversation.id, conversation.peerUser.id)
                                navController.navigate("chat/${conversation.id}/${conversation.peerUser.id}") {
                                    // Ao navegar para o chat, mantenha a lista na backstack e permita voltar corretamente
                                    popUpTo("chat") { inclusive = false }
                                }
                            })
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationListItem(conversation: Conversation, onClick: () -> Unit) {
    // Item de conversa com avatar placeholder, nome, última mensagem, horário e badge de não lidas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder circular
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(RoyalBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.peerUser.name.first().toString(),
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = conversation.peerUser.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = formatTime(conversation.lastTimestamp), fontSize = 12.sp, color = Color.Gray)
            }

            Text(
                text = conversation.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (conversation.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Badge(containerColor = RoyalBlue, contentColor = White) {
                Text(text = conversation.unreadCount.toString())
            }
        }
    }
}

// Utilidade para formatar timestamp em "HH:mm" (exibido na lista)
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun ConversationListPreview() {
    WtcCrmTheme {
        // preview sem NavController: mostra somente lista
        ConversationListScreen(navController = rememberNavControllerPreview())
    }
}

// Helper para preview (não resolve NavController na preview normal)
@Composable
fun rememberNavControllerPreview(): NavController {
    // retorna um NavController fake para previews — na prática o preview não navegará
    return androidx.navigation.compose.rememberNavController()
}
