package br.com.fiap.wtcconnect.data.model

data class Client(
    val id: Int,
    val name: String,
    val status: String,
    val tags: List<String>,
    val score: Int
)