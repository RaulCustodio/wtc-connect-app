package br.com.fiap.wtcconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.fiap.wtcconnect.data.ChatRepository
import br.com.fiap.wtcconnect.data.FakeChatRepository
import br.com.fiap.wtcconnect.presentation.ChatViewModel
import br.com.fiap.wtcconnect.presentation.ChatViewModelFactory
import br.com.fiap.wtcconnect.data.Message
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import br.com.fiap.wtcconnect.ui.theme.RoyalBlue

/**
 * ChatScreen composable que exibe o histórico de mensagens para a conversationId fornecida.
 * Recebe NavController para permitir voltar à lista com popUpTo correto.
 * Usa ChatViewModel para carregar mensagens via Flow exposto pelo repositório.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, conversationId: String, peerUserId: String, repository: ChatRepository? = null, currentUserId: String? = null, currentUserType: br.com.fiap.wtcconnect.viewmodel.UserType = br.com.fiap.wtcconnect.viewmodel.UserType.CLIENT) {
    val repo = repository ?: FakeChatRepository(currentUserId = currentUserId ?: "me")
    // Cria ViewModel com factory passando conversationId e currentUserId
    val effectiveUserId = currentUserId ?: "me"
    val vm: ChatViewModel = viewModel(factory = ChatViewModelFactory(repo, conversationId, effectiveUserId))
    val state by vm.uiState.collectAsState()

    // Tenta obter nome do peer a partir das conversas disponíveis
    val conversations by repo.getConversations().collectAsState(initial = emptyList())
    val peerName = conversations.find { it.id == conversationId }?.peerUser?.name ?: "Contato"

    // Authorization checks
    var authorized by remember { mutableStateOf(true) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(conversationId, currentUserId, currentUserType) {
        // Group chat: peerUserId is in format "group:{groupId}" or conversationId startsWith("group_")
        if (conversationId.startsWith("group_") || peerUserId.startsWith("group:")) {
            val groupId = peerUserId.removePrefix("group:").ifEmpty { conversationId.removePrefix("group_") }
            // check membership
            val userGroup = currentUserId?.let { uid -> repo.getUserGroupId(uid) }
            var gid: String? = null
            if (userGroup != null) {
                userGroup.collect { gid = it }
            }
            if (gid != groupId && currentUserType != br.com.fiap.wtcconnect.viewmodel.UserType.OPERATOR) {
                authorized = false
                authErrorMessage = "Você não tem permissão para ver o chat deste grupo"
            }
        } else {
            // 1:1 chat: ensure both users are in same group if current is CLIENT
            if (currentUserType == br.com.fiap.wtcconnect.viewmodel.UserType.CLIENT && currentUserId != null) {
                val myGroup = repo.getUserGroupId(currentUserId)
                var gid: String? = null
                myGroup.collect { gid = it }
                val peerGroupFlow = repo.getUserGroupId(peerUserId)
                var peerGid: String? = null
                peerGroupFlow.collect { peerGid = it }
                if (gid == null || peerGid == null || gid != peerGid) {
                    authorized = false
                    authErrorMessage = "Você só pode conversar com membros do seu grupo"
                }
            }
        }
    }

    if (!authorized) {
        // Exibe mensagem de erro clara
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = authErrorMessage ?: "Acesso negado", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.navigateUp() }) { Text("Voltar") }
            }
        }
        return
    }

    val listState = rememberLazyListState()

    // Rolar para o fim no carregamento inicial e sempre que mensagens mudarem
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            // scroll até a última mensagem
            listState.scrollToItem(state.messages.size - 1)
        }
    }

    // Rolar para a última mensagem assim que o loading terminar (carregamento inicial)
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.size - 1)
        }
    }

    var input by remember { mutableStateOf("") }

    // Cache simples de usuários para exibir nome do remetente
    val userCache = remember { mutableStateMapOf<String, br.com.fiap.wtcconnect.data.User?>() }

    // Referencia peerUserId para evitar warning de "parameter never used" (no futuro será usado para carregar perfil)
    LaunchedEffect(peerUserId) { /* noop: peerUserId observado para futuras mudanças */ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = peerName.firstOrNull()?.toString() ?: "C", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(text = peerName, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                         // Volta para a lista garantindo popUpTo correto
                         navController.navigate("chat") {
                             popUpTo("chat") { inclusive = false }
                         }
                     }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Erro: ${state.error}")
                }
                else -> {
                    // Lista de mensagens
                    LazyColumn(
                         modifier = Modifier
                             .weight(1f)
                             .fillMaxWidth()
                             .padding(8.dp),
                         state = listState
                     ) {
                         items(state.messages) { message ->
                             // isMe: identifica se a mensagem foi enviada pelo usuário local (fake id "me")
                             val isMe = message.senderId == (currentUserId ?: "me")
                             val senderName = if (isMe) null else userCache[message.senderId]
                                 ?: run {
                                     // inicia coleta em background para popular cache
                                     LaunchedEffect(message.senderId) {
                                         repo.getUserById(message.senderId).collect { u ->
                                             userCache[message.senderId] = u
                                         }
                                     }
                                     userCache[message.senderId]
                                 }

                             MessageRow(message = message, isMe = isMe, senderName = senderName?.name)
                         }
                     }

                    // Entrada de texto + botão enviar
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Digite uma mensagem")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            val content = input.trim()
                            if (content.isNotEmpty()) {
                                vm.sendMessage(content)
                                input = ""
                                // A rolagem será tratada pelo LaunchedEffect que observa state.messages.size
                            }
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageRow(message: Message, isMe: Boolean, senderName: String? = null) {
    // Layout de mensagem com avatar para mensagens recebidas
    val bg = if (isMe) RoyalBlue else Color(0xFFEFEFEF)
    if (isMe) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Column(modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(4.dp)
                .background(bg, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)) {
                Text(text = message.content, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formatTime(message.createdAt), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.End))
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
            // Avatar com iniciais
            val initials = senderName?.firstOrNull()?.toString() ?: message.senderId.firstOrNull()?.toString() ?: "U"
            Box(modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RoyalBlue), contentAlignment = Alignment.Center) {
                Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(4.dp)
                .background(bg, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)) {
                if (!senderName.isNullOrEmpty()) {
                    Text(text = senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(text = message.content, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formatTime(message.createdAt), fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
