package com.example.futebolamador.repository

import com.example.futebolamador.data.TimeEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TimeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val colecao = db.collection("times")

    suspend fun getTodos(): List<TimeEntity> {
        return try {
            val resultado = colecao.orderBy("nome").get().await()
            resultado.documents.mapNotNull { doc ->
                TimeEntity(
                    id = doc.getString("id") ?: doc.id,
                    nome = doc.getString("nome") ?: "",
                    cidade = doc.getString("cidade") ?: "",
                    dataFundacao = doc.getString("dataFundacao") ?: "",
                    brasaoUri = doc.getString("brasaoUri") ?: ""
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun inserir(time: TimeEntity): String {
        val ref = colecao.document()
        ref.set(mapOf(
            "id" to ref.id,
            "nome" to time.nome,
            "cidade" to time.cidade,
            "dataFundacao" to time.dataFundacao,
            "brasaoUri" to time.brasaoUri
        )).await()
        return ref.id
    }

    suspend fun atualizar(time: TimeEntity) {
        colecao.document(time.id).set(mapOf(
            "id" to time.id,
            "nome" to time.nome,
            "cidade" to time.cidade,
            "dataFundacao" to time.dataFundacao,
            "brasaoUri" to time.brasaoUri
        )).await()
    }

    suspend fun deletar(time: TimeEntity) {
        colecao.document(time.id).delete().await()
    }

    suspend fun getPorId(id: String): TimeEntity? {
        return try {
            val doc = colecao.document(id).get().await()
            if (doc.exists()) {
                TimeEntity(
                    id = doc.getString("id") ?: doc.id,
                    nome = doc.getString("nome") ?: "",
                    cidade = doc.getString("cidade") ?: "",
                    dataFundacao = doc.getString("dataFundacao") ?: "",
                    brasaoUri = doc.getString("brasaoUri") ?: ""
                )
            } else null
        } catch (e: Exception) { null }
    }
}