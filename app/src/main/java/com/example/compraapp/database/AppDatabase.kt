package com.example.compraapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.compraapp.database.Producto

@Database(entities = [Producto::class], version = 1)
// LA ANOTACIÓN @Database MARCA ESTA CLASE COMO UNA BASE DE DATOS ROOM.
// "entities = [Producto::class]" ESPECIFICA LAS TABLAS (ENTIDADES) QUE CONTIENE LA BASE DE DATOS.
// "version = 1" ES LA VERSIÓN DE LA BASE DE DATOS. DEBE INCREMENTARSE SI SE MODIFICA EL ESQUEMA.
abstract class AppDatabase : RoomDatabase() {
    // ESTA CLASE ABSTRACTA EXTIENDE RoomDatabase, LA CLASE BASE PARA LAS BASES DE DATOS ROOM.

    abstract fun productoDao(): ProductoDao
    // ESTA FUNCIÓN ABSTRACTA DEBE DEVOLVER UNA INSTANCIA DE ProductoDao.
    // ROOM PROPORCIONARÁ LA IMPLEMENTACIÓN DE ESTE MÉTODO.

    companion object {
        // EL COMPANION OBJECT PERMITE ACCEDER A LOS MIEMBROS DE ESTA CLASE DIRECTAMENTE A TRAVÉS DEL NOMBRE DE LA CLASE (EJ: AppDatabase.getDatabase(...)).
        @Volatile
        // @Volatile GARANTIZA QUE EL VALOR DE INSTANCE SEA SIEMPRE COHERENTE ENTRE DIFERENTES HILOS.
        private var INSTANCE: AppDatabase? = null
        // INSTANCE MANTIENE LA ÚNICA INSTANCIA DE LA BASE DE DATOS.

        fun getDatabase(context: Context): AppDatabase {
            // ESTA FUNCIÓN ESTÁTICA (ACCESIBLE DESDE FUERA) SE UTILIZA PARA OBTENER LA INSTANCIA DE LA BASE DE DATOS.
            return INSTANCE ?: synchronized(this) {
                // synchronized(this) ASEGURA QUE SOLO UN HILO A LA VEZ PUEDA EJECUTAR EL CÓDIGO DENTRO DE ESTE BLOQUE, EVITANDO CONDICIONES DE CARRERA AL CREAR LA INSTANCIA.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "compra_app_database" // EL NOMBRE DEL ARCHIVO DE LA BASE DE DATOS EN EL DISPOSITIVO.
                )
                    .build()
                INSTANCE = instance
                // SE ASIGNA LA INSTANCIA RECIÉN CREADA A LA VARIABLE INSTANCE.
                instance
                // SE DEVUELVE LA INSTANCIA DE LA BASE DE DATOS.
            }
        }
    }
}