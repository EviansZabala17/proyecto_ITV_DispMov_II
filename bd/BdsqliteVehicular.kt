package com.example.ejer_bd_p

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BdsqliteVehicular(context: Context) : SQLiteOpenHelper(context, "inspeccion.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla Técnico
        db.execSQL("""
            CREATE TABLE tecnico (
                ci TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                telefono TEXT NOT NULL
            )
        """.trimIndent())

        // Tabla Automóvil
        db.execSQL("""
            CREATE TABLE automovil (
                placa TEXT PRIMARY KEY,
                marca TEXT NOT NULL,
                modelo TEXT NOT NULL,
                anio INTEGER NOT NULL
            )
        """.trimIndent())

        // Tabla de relación Inspección (N:M)
        db.execSQL("""
            CREATE TABLE inspecciona (
                id_tecnico TEXT NOT NULL,
                id_automovil TEXT NOT NULL,
                fecha TEXT NOT NULL,
                observaciones TEXT,
                estado TEXT NOT NULL,
                PRIMARY KEY (id_tecnico, id_automovil, fecha),
                FOREIGN KEY (id_tecnico) REFERENCES tecnico(ci) ON DELETE CASCADE,
                FOREIGN KEY (id_automovil) REFERENCES automovil(placa) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Si cambias la estructura de las tablas, aquí puedes manejar la migración
        db.execSQL("DROP TABLE IF EXISTS inspecciona")
        db.execSQL("DROP TABLE IF EXISTS automovil")
        db.execSQL("DROP TABLE IF EXISTS tecnico")
        onCreate(db)
    }
}
