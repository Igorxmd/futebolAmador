package com.example.futebolamador.ui.times

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.futebolamador.data.TimeEntity
import com.example.futebolamador.repository.TimeRepository

class TimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TimeRepository(application)

    // MutableLiveData para a tela observar as mudanças
    val todos = MutableLiveData<List<TimeEntity>>()

    init {
        carregarTodos()
    }

    fun carregarTodos() {
        todos.value = repository.getTodos()
    }

    fun inserir(time: TimeEntity) {
        repository.inserir(time)
        carregarTodos()
    }

    fun atualizar(time: TimeEntity) {
        repository.atualizar(time)
        carregarTodos()
    }

    fun deletar(time: TimeEntity) {
        repository.deletar(time)
        carregarTodos()
    }

    fun getPorId(id: Int): TimeEntity? {
        return repository.getPorId(id)
    }
}