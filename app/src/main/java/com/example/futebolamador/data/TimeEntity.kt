package com.example.futebolamador.data

data class TimeEntity(
    val id: Int = 0,
    val nome: String,
    val cidade: String,
    val dataFundacao: String = "",
    val brasaoUri: String = ""
)