package com.example.futebolamador.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SessaoManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun usuarioAtualId(): String? = auth.currentUser?.uid
    fun usuarioAtualEmail(): String? = auth.currentUser?.email

    suspend fun perfilAtual(): String {
        val uid = usuarioAtualId() ?: return "torcedor"
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            doc.getString("perfil") ?: "torcedor"
        } catch (e: Exception) { "torcedor" }
    }

    suspend fun dadosUsuario(): Map<String, String> {
        val uid = usuarioAtualId() ?: return emptyMap()
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            mapOf(
                "nome" to (doc.getString("nome") ?: ""),
                "email" to (doc.getString("email") ?: ""),
                "perfil" to (doc.getString("perfil") ?: "torcedor"),
                "fotoUri" to (doc.getString("fotoUri") ?: ""),
                "pontos" to (doc.getLong("pontos") ?: 0L).toString()
            )
        } catch (e: Exception) { emptyMap() }
    }

    suspend fun atualizarPerfil(nome: String, fotoUri: String) {
        val uid = usuarioAtualId() ?: return
        db.collection("usuarios").document(uid)
            .update(mapOf("nome" to nome, "fotoUri" to fotoUri)).await()
    }

    suspend fun adicionarPontos(quantidade: Int) {
        val uid = usuarioAtualId() ?: return
        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            val pontosAtuais = doc.getLong("pontos") ?: 0L
            db.collection("usuarios").document(uid)
                .update("pontos", pontosAtuais + quantidade).await()
        } catch (e: Exception) {}
    }

    suspend fun getRanking(): List<Map<String, String>> {
        return try {
            val resultado = db.collection("usuarios")
                .orderBy("pontos", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get().await()
            resultado.documents.map { doc ->
                mapOf(
                    "nome" to (doc.getString("nome") ?: "Usuário"),
                    "pontos" to (doc.getLong("pontos") ?: 0L).toString(),
                    "perfil" to (doc.getString("perfil") ?: "torcedor")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun enviarSolicitacao(): Boolean {
        val uid = usuarioAtualId() ?: return false
        return try {
            val dados = dadosUsuario()
            val existente = db.collection("solicitacoes")
                .whereEqualTo("uid", uid)
                .whereEqualTo("status", "pendente")
                .get().await()
            if (!existente.isEmpty) return false
            db.collection("solicitacoes").add(mapOf(
                "uid" to uid,
                "nome" to dados["nome"],
                "email" to dados["email"],
                "status" to "pendente",
                "timestamp" to com.google.firebase.Timestamp.now()
            )).await()
            true
        } catch (e: Exception) { false }
    }

    fun logout() { auth.signOut() }
}