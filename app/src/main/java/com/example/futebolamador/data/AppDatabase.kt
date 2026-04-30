package com.example.futebolamador.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "futebol_database", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE times (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cidade TEXT NOT NULL,
                dataFundacao TEXT DEFAULT '',
                brasaoUri TEXT DEFAULT ''
            )
        """)
        db.execSQL("""
            CREATE TABLE jogos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timeMandanteId INTEGER NOT NULL,
                timeVisitanteId INTEGER NOT NULL,
                timeMandanteNome TEXT NOT NULL,
                timeVisitanteNome TEXT NOT NULL,
                data TEXT NOT NULL,
                hora TEXT NOT NULL,
                placarMandante INTEGER DEFAULT 0,
                placarVisitante INTEGER DEFAULT 0,
                status TEXT DEFAULT 'Agendado',
                horarioInicio TEXT DEFAULT '',
                horarioIntervalo TEXT DEFAULT '',
                horarioFim TEXT DEFAULT ''
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS times")
        db.execSQL("DROP TABLE IF EXISTS jogos")
        onCreate(db)
    }
}