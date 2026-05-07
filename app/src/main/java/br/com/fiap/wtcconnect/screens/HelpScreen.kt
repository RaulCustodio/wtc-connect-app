package br.com.fiap.wtcconnect.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme

data class FaqItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit = {}
) {
    val faqItems = remember {
        listOf(
            FaqItem(
                question = "Como faço para enviar uma mensagem?",
                answer = "Para enviar uma mensagem, basta acessar a tela de conversas, selecionar um contato e digitar sua mensagem no campo de texto na parte inferior da tela. Pressione o botão de enviar para compartilhar."
            ),
            FaqItem(
                question = "Posso deletar mensagens enviadas?",
                answer = "Sim, você pode deletar mensagens enviadas. Basta pressionar e segurar a mensagem que deseja excluir, e então selecionar a opção 'Deletar' no menu que aparece."
            ),
            FaqItem(
                question = "Como altero minha foto de perfil?",
                answer = "Vá para a tela de Perfil, toque no ícone de câmera sobre sua foto atual e selecione uma nova imagem da galeria do seu dispositivo."
            ),
            FaqItem(
                question = "Como desativo as notificações?",
                answer = "Acesse Configurações > Notificações e desative as opções de acordo com sua preferência. Você pode desativar completamente ou escolher quais tipos de notificações deseja receber."
            ),
            FaqItem(
                question = "O app funciona sem internet?",
                answer = "Não, o aplicativo requer conexão com a internet para enviar e receber mensagens em tempo real. Mensagens enviadas sem internet serão armazenadas e enviadas assim que a conexão for restabelecida."
            ),
            FaqItem(
                question = "Como faço para trocar minha senha?",
                answer = "Acesse Perfil e Configurações > Trocar Senha. Digite sua senha atual, insira a nova senha duas vezes e clique em 'Alterar Senha'. Certifique-se de que a nova senha atende aos requisitos de segurança."
            ),
            FaqItem(
                question = "Posso usar o app em vários dispositivos?",
                answer = "Sim, você pode usar sua conta em múltiplos dispositivos. Basta fazer login com suas credenciais em cada dispositivo. Suas conversas serão sincronizadas automaticamente."
            ),
            FaqItem(
                question = "Como reporto um problema ou bug?",
                answer = "Se você encontrar algum problema, entre em contato conosco através do e-mail suporte@wtcconnect.com.br. Descreva o problema em detalhes e, se possível, inclua capturas de tela."
            ),
            FaqItem(
                question = "Meus dados estão seguros?",
                answer = "Sim, levamos a segurança muito a sério. Todas as mensagens são criptografadas de ponta a ponta e seus dados pessoais são protegidos de acordo com a LGPD (Lei Geral de Proteção de Dados)."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajuda e Suporte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Perguntas Frequentes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encontre respostas para as dúvidas mais comuns",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // FAQ Items
            Text(
                text = "Perguntas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    faqItems.forEachIndexed { index, item ->
                        FaqItemView(item = item)
                        if (index < faqItems.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            // Contato
            Text(
                text = "Ainda precisa de ajuda?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 32.dp, top = 24.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Entre em contato",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "E-mail: suporte@wtcconnect.com.br",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Horário de atendimento: Seg-Sex, 9h-18h",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FaqItemView(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = item.answer,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelpScreenPreview() {
    WtcCrmTheme {
        HelpScreen()
    }
}
