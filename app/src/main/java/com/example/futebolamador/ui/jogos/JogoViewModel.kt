package com.example.futebolamador.ui.jogos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.futebolamador.data.JogoEntity
import com.example.futebolamador.repository.JogoRepository

class JogoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JogoRepository(application)
    val todos = MutableLiveData<List<JogoEntity>>()

    init {
        carregarTodos()
    }

    fun carregarTodos() {
        todos.value = repository.getTodos()
    }

    fun inserir(jogo: JogoEntity) {
        repository.inserir(jogo)
        carregarTodos()
    }

    fun atualizar(jogo: JogoEntity) {
        repository.atualizar(jogo)
        carregarTodos()
    }

    fun deletar(jogo: JogoEntity) {
        repository.deletar(jogo)
        carregarTodos()
    }

    fun getPorId(id: Int): JogoEntity? = repository.getPorId(id)
}