package com.example.futebolamador.data

import android.content.ContentValues
import android.content.Context

class TimeDao(context: Context) {

    private val db = AppDatabase(context).writableDatabase

    fun getTodos(): List<TimeEntity> {
        val lista = mutableListOf<TimeEntity>()
        val cursor = db.query("times", null, null, null, null, null, "nome ASC")
        while (cursor.moveToNext()) {
            lista.add(
                TimeEntity(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    cidade = cursor.getString(cursor.getColumnIndexOrThrow("cidade")),
                    corPrimaria = cursor.getString(cursor.getColumnIndexOrThrow("corPrimaria"))
                )
            )
        }
        cursor.close()
        return lista
    }

    fun getPorId(id: Int): TimeEntity? {
        val cursor = db.query("times", null, "id=?", arrayOf(id.toString()), null, null, null)
        return if (cursor.moveToFirst()) {
            TimeEntity(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                cidade = cursor.getString(cursor.getColumnIndexOrThrow("cidade")),
                corPrimaria = cursor.getString(cursor.getColumnIndexOrThrow("corPrimaria"))
            ).also { cursor.close() }
        } else null
    }

    fun inserir(time: TimeEntity) {
        val values = ContentValues().apply {
            put("nome", time.nome)
            put("cidade", time.cidade)
            put("corPrimaria", time.corPrimaria)
        }
        db.insert("times", null, values)
    }

    fun atualizar(time: TimeEntity) {
        val values = ContentValues().apply {
            put("nome", time.nome)
            put("cidade", time.cidade)
            put("corPrimaria", time.corPrimaria)
        }
        db.update("times", values, "id=?", arrayOf(time.id.toString()))
    }

    fun deletar(time: TimeEntity) {
        db.delete("times", "id=?", arrayOf(time.id.toString()))
    }
}