package com.example.futebolamador.data

data class JogoEntity(
    val id: String = "",
    val timeMandanteId: String = "",
    val timeVisitanteId: String = "",
    val timeMandanteNome: String = "",
    val timeVisitanteNome: String = "",
    val data: String = "",
    val hora: String = "",
    val placarMandante: Int = 0,
    val placarVisitante: Int = 0,
    val status: String = "Agendado",
    val horarioInicio: String = "",
    val horarioIntervalo: String = "",
    val horarioFim: String = ""
)