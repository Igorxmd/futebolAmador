package com.example.futebolamador.ui.times

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.futebolamador.data.TimeEntity
import com.example.futebolamador.repository.TimeRepository
import kotlinx.coroutines.launch

class TimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TimeRepository()
    val todos = MutableLiveData<List<TimeEntity>>()

    init { carregarTodos() }

    fun carregarTodos() = viewModelScope.launch {
        todos.postValue(repository.getTodos())
    }

    fun inserir(time: TimeEntity) = viewModelScope.launch {
        repository.inserir(time)
        carregarTodos()
    }

    fun atualizar(time: TimeEntity) = viewModelScope.launch {
        repository.atualizar(time)
        carregarTodos()
    }

    fun deletar(time: TimeEntity) = viewModelScope.launch {
        repository.deletar(time)
        carregarTodos()
    }

    suspend fun getPorIdFirestore(id: String): TimeEntity? {
        return repository.getPorId(id)
    }
}