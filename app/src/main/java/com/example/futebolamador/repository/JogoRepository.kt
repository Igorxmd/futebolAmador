package com.example.futebolamador.repository

import com.example.futebolamador.data.JogoEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JogoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val colecao = db.collection("jogos")

    suspend fun getTodos(): List<JogoEntity> {
        return try {
            // orderBy("data") com DD/MM/AAAA ordena errado — ordenação feita no cliente
            val resultado = colecao.get().await()
            resultado.documents.mapNotNull { doc -> docParaJogo(doc) }
                .sortedWith(compareBy(
                    { it.data.split("/").let { p -> if (p.size == 3) "${p[2]}${p[1]}${p[0]}" else it.data } },
                    { it.hora }
                ))
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getPorId(id: String): JogoEntity? {
        return try {
            val doc = colecao.document(id).get().await()
            if (doc.exists()) docParaJogo(doc) else null
        } catch (e: Exception) { null }
    }

    private fun docParaJogo(doc: com.google.firebase.firestore.DocumentSnapshot) = JogoEntity(
        id = doc.getString("id") ?: doc.id,
        timeMandanteId   = doc.getString("timeMandanteId") ?: "",
        timeVisitanteId  = doc.getString("timeVisitanteId") ?: "",
        timeMandanteNome = doc.getString("timeMandanteNome") ?: "",
        timeVisitanteNome= doc.getString("timeVisitanteNome") ?: "",
        data             = doc.getString("data") ?: "",
        hora             = doc.getString("hora") ?: "",
        local            = doc.getString("local") ?: "",
        placarMandante   = (doc.getLong("placarMandante") ?: 0).toInt(),
        placarVisitante  = (doc.getLong("placarVisitante") ?: 0).toInt(),
        status           = doc.getString("status") ?: "Agendado",
        horarioInicio    = doc.getString("horarioInicio") ?: "",
        horarioIntervalo = doc.getString("horarioIntervalo") ?: "",
        horarioFim       = doc.getString("horarioFim") ?: ""
    )

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
        "id"               to jogo.id,
        "timeMandanteId"   to jogo.timeMandanteId,
        "timeVisitanteId"  to jogo.timeVisitanteId,
        "timeMandanteNome" to jogo.timeMandanteNome,
        "timeVisitanteNome" to jogo.timeVisitanteNome,
        "data"             to jogo.data,
        "hora"             to jogo.hora,
        "local"            to jogo.local,
        "placarMandante"   to jogo.placarMandante,
        "placarVisitante"  to jogo.placarVisitante,
        "status"           to jogo.status,
        "horarioInicio"    to jogo.horarioInicio,
        "horarioIntervalo" to jogo.horarioIntervalo,
        "horarioFim"       to jogo.horarioFim
    )

    fun calcularResultado(placarMandante: Int, placarVisitante: Int): String {
        return when {
            placarMandante > placarVisitante -> "mandante"
            placarMandante < placarVisitante -> "visitante"
            else -> "empate"
        }
    }
}