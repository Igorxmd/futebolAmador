package com.example.futebolamador.repository

import android.content.Context
import com.example.futebolamador.data.TimeDao
import com.example.futebolamador.data.TimeEntity

class TimeRepository(context: Context) {

    private val dao = TimeDao(context)

    fun getTodos(): List<TimeEntity> = dao.getTodos()

    fun inserir(time: TimeEntity) = dao.inserir(time)

    fun atualizar(time: TimeEntity) = dao.atualizar(time)

    fun deletar(time: TimeEntity) = dao.deletar(time)

    fun getPorId(id: Int): TimeEntity? = dao.getPorId(id)
}