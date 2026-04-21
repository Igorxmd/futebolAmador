package com.example.futebolamador.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity diz ao Room que essa classe é uma tabela no banco de dados
@Entity(tableName = "times")
data class TimeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,

    val cidade: String,

    // Cor usada para representar o time (ex: "#FF0000")
    val corPrimaria: String = "#1E88E5"
)
