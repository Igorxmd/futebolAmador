package com.example.futebolamador.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Aposta(
    val id: String = "",
    val uid: String = "",
    val jogoId: String = "",
    val palpite: String = "",
    val nomeUsuario: String = "",
    val pontosAtribuidos: Boolean = false
)

class ApostaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val colecao = db.collection("apostas")

    suspend fun meuPalpite(jogoId: String): Aposta? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val resultado = colecao
                .whereEqualTo("uid", uid)
                .whereEqualTo("jogoId", jogoId)
                .get().await()
            if (resultado.isEmpty) null
            else {
                val doc = resultado.documents[0]
                Aposta(
                    id = doc.id,
                    uid = doc.getString("uid") ?: "",
                    jogoId = doc.getString("jogoId") ?: "",
                    palpite = doc.getString("palpite") ?: "",
                    nomeUsuario = doc.getString("nomeUsuario") ?: "",
                    pontosAtribuidos = doc.getBoolean("pontosAtribuidos") ?: false
                )
            }
        } catch (e: Exception) { null }
    }

    suspend fun contarPalpites(jogoId: String): Map<String, Int> {
        return try {
            val resultado = colecao.whereEqualTo("jogoId", jogoId).get().await()
            val contagem = mutableMapOf("mandante" to 0, "empate" to 0, "visitante" to 0)
            resultado.documents.forEach { doc ->
                val palpite = doc.getString("palpite") ?: ""
                contagem[palpite] = (contagem[palpite] ?: 0) + 1
            }
            contagem
        } catch (e: Exception) { mapOf("mandante" to 0, "empate" to 0, "visitante" to 0) }
    }

    suspend fun apostar(jogoId: String, palpite: String, nomeUsuario: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val existente = meuPalpite(jogoId)
            if (existente != null) {
                colecao.document(existente.id).update("palpite", palpite).await()
            } else {
                colecao.add(mapOf(
                    "uid" to uid,
                    "jogoId" to jogoId,
                    "palpite" to palpite,
                    "nomeUsuario" to nomeUsuario,
                    "pontosAtribuidos" to false
                )).await()
            }
            true
        } catch (e: Exception) { false }
    }

    suspend fun cancelarAposta(jogoId: String): Boolean {
        return try {
            val existente = meuPalpite(jogoId) ?: return false
            colecao.document(existente.id).delete().await()
            true
        } catch (e: Exception) { false }
    }

    // Atribui pontos ao usuário se acertou e ainda não recebeu
    suspend fun verificarEAtribuirPontos(
        aposta: Aposta,
        resultadoReal: String,
        onPontosAtribuidos: suspend () -> Unit
    ) {
        if (aposta.pontosAtribuidos) return
        if (aposta.palpite == resultadoReal) {
            // Marca como atribuído primeiro para evitar duplicata
            colecao.document(aposta.id)
                .update("pontosAtribuidos", true).await()
            // Adiciona 10 pontos ao usuário
            onPontosAtribuidos()
        } else {
            // Errou — apenas marca como processado
            colecao.document(aposta.id)
                .update("pontosAtribuidos", true).await()
        }
    }

    fun calcularResultado(placarMandante: Int, placarVisitante: Int): String {
        return when {
            placarMandante > placarVisitante -> "mandante"
            placarMandante < placarVisitante -> "visitante"
            else -> "empate"
        }
    }
}