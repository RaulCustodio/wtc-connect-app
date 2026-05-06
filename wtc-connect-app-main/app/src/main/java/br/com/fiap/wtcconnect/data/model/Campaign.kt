package br.com.fiap.wtcconnect.data.model

data class Campaign(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val segment: String,
    val imageUrl: Int?
)