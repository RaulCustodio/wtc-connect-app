package br.com.fiap.wtcconnect.screens.campaigns

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcconnect.R
import br.com.fiap.wtcconnect.data.model.Campaign
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme

val mockCampaigns = listOf(
    Campaign(1, "Feirão WTC", "Até 40% de desconto para associados", "15-20 OUT", "Todos", R.drawable.desconto),
    Campaign(2, "WTC Summit 2024", "O maior evento de networking do ano.", "25 NOV", "VIP", R.drawable.networking),
    Campaign(3, "Happy Hour de Negócios", "Conecte-se com líderes do mercado.", "Toda Sexta", "Lead Quente", R.drawable.negocios)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen() {

    val campaigns = remember {
        mutableStateListOf<Campaign>().apply {
            addAll(mockCampaigns)
        }
    }

    var showNewCampaignDialog by remember { mutableStateOf(false) }
    var editingCampaign by remember { mutableStateOf<Campaign?>(null) }

    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf("") }
    var newSegment by remember { mutableStateOf("Todos") }

    // Preencher dados ao editar
    LaunchedEffect(editingCampaign) {
        editingCampaign?.let {
            newTitle = it.title
            newDescription = it.description
            newDate = it.date
            newSegment = it.segment
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campanhas Recentes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCampaign = null
                showNewCampaignDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Campanha")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(campaigns, key = { it.id }) { campaign ->
                CampaignCardExpandable(
                    campaign = campaign,
                    onEdit = {
                        editingCampaign = campaign
                        showNewCampaignDialog = true
                    }
                )
            }
        }
    }

    if (showNewCampaignDialog) {
        AlertDialog(
            onDismissRequest = { showNewCampaignDialog = false },
            title = { Text(if (editingCampaign != null) "Editar Campanha" else "Nova Campanha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("Data") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newSegment,
                        onValueChange = { newSegment = it },
                        label = { Text("Segmento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {

                    if (newTitle.isNotBlank() && newDescription.isNotBlank() && newDate.isNotBlank()) {

                        if (editingCampaign != null) {
                            // EDITAR
                            val index = campaigns.indexOfFirst { it.id == editingCampaign!!.id }

                            if (index != -1) {
                                campaigns[index] = Campaign(
                                    id = editingCampaign!!.id,
                                    title = newTitle.trim(),
                                    description = newDescription.trim(),
                                    date = newDate.trim(),
                                    segment = newSegment.trim().ifBlank { "Todos" },
                                    imageUrl = editingCampaign!!.imageUrl
                                )
                            }

                        } else {
                            // NOVO
                            val nextId = (campaigns.maxOfOrNull { it.id } ?: 0) + 1

                            campaigns.add(
                                Campaign(
                                    id = nextId,
                                    title = newTitle.trim(),
                                    description = newDescription.trim(),
                                    date = newDate.trim(),
                                    segment = newSegment.trim().ifBlank { "Todos" },
                                    imageUrl = null
                                )
                            )
                        }

                        // limpar
                        newTitle = ""
                        newDescription = ""
                        newDate = ""
                        newSegment = "Todos"
                        editingCampaign = null
                        showNewCampaignDialog = false
                    }

                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editingCampaign = null
                    showNewCampaignDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CampaignCardExpandable(
    campaign: Campaign,
    onEdit: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isConfirmed by rememberSaveable { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            campaign.imageUrl?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = campaign.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {

                Text(campaign.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Segmento: ${campaign.segment}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(campaign.description, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(campaign.date, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Editar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            isConfirmed = true
                            showMessage = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isConfirmed) "Confirmado" else "Confirmar presença")
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Detalhes da campanha: ${campaign.description}")
                }

                if (showMessage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Presença confirmada!", color = Color.Green)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampaignsScreenPreview() {
    WtcCrmTheme {
        CampaignsScreen()
    }
}