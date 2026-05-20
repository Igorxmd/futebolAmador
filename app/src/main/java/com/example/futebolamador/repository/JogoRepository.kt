package com.example.futebolamador.repository

import com.example.futebolamador.data.JogoEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JogoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val colecao = db.collection("jogos")

    suspend fun getTodos(): List<JogoEntity> {
        return try {
            val resultado = colecao.orderBy("data").get().await()
            resultado.documents.mapNotNull { doc ->
                JogoEntity(
                    id = doc.getString("id") ?: doc.id,
                    timeMandanteId = doc.getString("timeMandanteId") ?: "",
                    timeVisitanteId = doc.getString("timeVisitanteId") ?: "",
                    timeMandanteNome = doc.getString("timeMandanteNome") ?: "",
                    timeVisitanteNome = doc.getString("timeVisitanteNome") ?: "",
                    data = doc.getString("data") ?: "",
                    hora = doc.getString("hora") ?: "",
                    placarMandante = (doc.getLong("placarMandante") ?: 0).toInt(),
                    placarVisitante = (doc.getLong("placarVisitante") ?: 0).toInt(),
                    status = doc.getString("status") ?: "Agendado",
                    horarioInicio = doc.getString("horarioInicio") ?: "",
                    horarioIntervalo = doc.getString("horarioIntervalo") ?: "",
                    horarioFim = doc.getString("horarioFim") ?: ""
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun inserir(jogo: JogoEntity): String {
        val ref = colecao.document()
        ref.set(jogoParaMapa(jogo.copy(id = ref.id))).await()
        return ref.id
    }

    suspend fun atualizar(jogo: JogoEntity) {
        colecao.document(jogo.id).set(jogoParaMapa(jogo)).await()
    }

    suspend fun deletar(jogo: JogoEntity) {
        colecao.document(jogo.id).delete().await()
    }

    private fun jogoParaMapa(jogo: JogoEntity) = mapOf(
        "id" to jogo.id,
        "timeMandanteId" to jogo.timeMandanteId,
        "timeVisitanteId" to jogo.timeVisitanteId,
        "timeMandanteNome" to jogo.timeMandanteNome,
        "timeVisitanteNome" to jogo.timeVisitanteNome,
        "data" to jogo.data,
        "hora" to jogo.hora,
        "placarMandante" to jogo.placarMandante,
        "placarVisitante" to jogo.placarVisitante,
        "status" to jogo.status,
        "horarioInicio" to jogo.horarioInicio,
        "horarioIntervalo" to jogo.horarioIntervalo,
        "horarioFim" to jogo.horarioFim
    )
    suspend fun getPorId(id: String): JogoEntity? {
        return try {
            val doc = colecao.document(id).get().await()
            if (doc.exists()) {
                JogoEntity(
                    id = doc.getString("id") ?: doc.id,
                    timeMandanteId = doc.getString("timeMandanteId") ?: "",
                    timeVisitanteId = doc.getString("timeVisitanteId") ?: "",
                    timeMandanteNome = doc.getString("timeMandanteNome") ?: "",
                    timeVisitanteNome = doc.getString("timeVisitanteNome") ?: "",
                    data = doc.getString("data") ?: "",
                    hora = doc.getString("hora") ?: "",
                    placarMandante = (doc.getLong("placarMandante") ?: 0).toInt(),
                    placarVisitante = (doc.getLong("placarVisitante") ?: 0).toInt(),
                    status = doc.getString("status") ?: "Agendado",
                    horarioInicio = doc.getString("horarioInicio") ?: "",
                    horarioIntervalo = doc.getString("horarioIntervalo") ?: "",
                    horarioFim = doc.getString("horarioFim") ?: ""
                )
            } else null
        } catch (e: Exception) { null }
    }
}