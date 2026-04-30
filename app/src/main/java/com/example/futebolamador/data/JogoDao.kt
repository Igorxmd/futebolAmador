package com.example.futebolamador.data

import android.content.ContentValues
import android.content.Context

class JogoDao(private val context: Context) {

    private val db get() = AppDatabase(context).writableDatabase

    fun getTodos(): List<JogoEntity> {
        val lista = mutableListOf<JogoEntity>()
        val cursor = db.query("jogos", null, null, null, null, null, "data ASC")
        while (cursor.moveToNext()) {
            lista.add(cursorToJogo(cursor))
        }
        cursor.close()
        return lista
    }

    fun getPorId(id: Int): JogoEntity? {
        val cursor = db.query("jogos", null, "id=?", arrayOf(id.toString()), null, null, null)
        return if (cursor.moveToFirst()) cursorToJogo(cursor).also { cursor.close() } else null
    }

    private fun cursorToJogo(cursor: android.database.Cursor) = JogoEntity(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        timeMandanteId = cursor.getInt(cursor.getColumnIndexOrThrow("timeMandanteId")),
        timeVisitanteId = cursor.getInt(cursor.getColumnIndexOrThrow("timeVisitanteId")),
        timeMandanteNome = cursor.getString(cursor.getColumnIndexOrThrow("timeMandanteNome")),
        timeVisitanteNome = cursor.getString(cursor.getColumnIndexOrThrow("timeVisitanteNome")),
        data = cursor.getString(cursor.getColumnIndexOrThrow("data")),
        hora = cursor.getString(cursor.getColumnIndexOrThrow("hora")),
        placarMandante = cursor.getInt(cursor.getColumnIndexOrThrow("placarMandante")),
        placarVisitante = cursor.getInt(cursor.getColumnIndexOrThrow("placarVisitante")),
        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
        horarioInicio = cursor.getString(cursor.getColumnIndexOrThrow("horarioInicio")),
        horarioIntervalo = cursor.getString(cursor.getColumnIndexOrThrow("horarioIntervalo")),
        horarioFim = cursor.getString(cursor.getColumnIndexOrThrow("horarioFim"))
    )

    fun inserir(jogo: JogoEntity) {
        db.insert("jogos", null, jogoToValues(jogo))
    }

    fun atualizar(jogo: JogoEntity) {
        db.update("jogos", jogoToValues(jogo), "id=?", arrayOf(jogo.id.toString()))
    }

    fun deletar(jogo: JogoEntity) {
        db.delete("jogos", "id=?", arrayOf(jogo.id.toString()))
    }

    private fun jogoToValues(jogo: JogoEntity) = ContentValues().apply {
        put("timeMandanteId", jogo.timeMandanteId)
        put("timeVisitanteId", jogo.timeVisitanteId)
        put("timeMandanteNome", jogo.timeMandanteNome)
        put("timeVisitanteNome", jogo.timeVisitanteNome)
        put("data", jogo.data)
        put("hora", jogo.hora)
        put("placarMandante", jogo.placarMandante)
        put("placarVisitante", jogo.placarVisitante)
        put("status", jogo.status)
        put("horarioInicio", jogo.horarioInicio)
        put("horarioIntervalo", jogo.horarioIntervalo)
        put("horarioFim", jogo.horarioFim)
    }
}