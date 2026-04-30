package com.example.futebolamador.repository

import android.content.Context
import com.example.futebolamador.data.JogoDao
import com.example.futebolamador.data.JogoEntity

class JogoRepository(context: Context) {

    private val dao = JogoDao(context)

    fun getTodos(): List<JogoEntity> = dao.getTodos()

    fun inserir(jogo: JogoEntity) = dao.inserir(jogo)

    fun atualizar(jogo: JogoEntity) = dao.atualizar(jogo)

    fun deletar(jogo: JogoEntity) = dao.deletar(jogo)

    fun getPorId(id: Int): JogoEntity? = dao.getPorId(id)
}