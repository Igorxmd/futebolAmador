package com.example.futebolamador.ui.jogos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.futebolamador.data.JogoEntity
import com.example.futebolamador.repository.JogoRepository
import kotlinx.coroutines.launch

class JogoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JogoRepository()
    val todos = MutableLiveData<List<JogoEntity>>()

    init { carregarTodos() }

    fun carregarTodos() = viewModelScope.launch {
        todos.postValue(repository.getTodos())
    }

    fun inserir(jogo: JogoEntity) = viewModelScope.launch {
        repository.inserir(jogo)
        carregarTodos()
    }

    fun atualizar(jogo: JogoEntity) = viewModelScope.launch {
        repository.atualizar(jogo)
        carregarTodos()
    }

    fun deletar(jogo: JogoEntity) = viewModelScope.launch {
        repository.deletar(jogo)
        carregarTodos()
    }

    suspend fun getPorIdFirestore(id: String): JogoEntity? {
        return repository.getPorId(id)
    }
}