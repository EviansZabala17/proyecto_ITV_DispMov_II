package com.example.ejer_bd_p

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BdSqlite(context: Context) : SQLiteOpenHelper(context, "inspeccion.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla Técnico
        db.execSQL("""
            CREATE TABLE tecnico (
                ci TEXT PRIMARY KEY CHECK (LENGTH(ci) BETWEEN 8 AND 12 AND ci GLOB '[0-9]*'),
                nombre TEXT NOT NULL,
                telefono TEXT NOT NULL CHECK (telefono GLOB '[0-9+]*')
            )
        """.trimIndent())

        // Tabla Automóvil
        db.execSQL("""
            CREATE TABLE automovil (
                placa TEXT PRIMARY KEY CHECK (placa GLOB '[A-Z][A-Z][A-Z]-[0-9][0-9][0-9]'),
                marca TEXT NOT NULL,
                modelo TEXT NOT NULL,
                anio INTEGER NOT NULL CHECK (anio BETWEEN 1900 AND 2025)
            )
        """.trimIndent())

        // Tabla Inspección
        db.execSQL("""
            CREATE TABLE inspecciona (
                id_inspeccion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_tecnico TEXT NOT NULL CHECK (LENGTH(id_tecnico) BETWEEN 8 AND 12 AND id_tecnico GLOB '[0-9]*'),
                id_automovil TEXT NOT NULL CHECK (id_automovil GLOB '[A-Z][A-Z][A-Z]-[0-9][0-9][0-9]'),
                fecha TEXT NOT NULL,
                detalles TEXT,
                resultado TEXT NOT NULL CHECK (LENGTH(resultado) >= 5),
                FOREIGN KEY (id_tecnico) REFERENCES tecnico(ci) ON DELETE CASCADE,
                FOREIGN KEY (id_automovil) REFERENCES automovil(placa) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS inspecciona")
        db.execSQL("DROP TABLE IF EXISTS automovil")
        db.execSQL("DROP TABLE IF EXISTS tecnico")
        onCreate(db)
    }
}