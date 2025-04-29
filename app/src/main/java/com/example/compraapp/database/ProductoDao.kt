package com.example.compraapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.compraapp.database.Producto
import kotlinx.coroutines.flow.Flow

@Dao // LA ANOTACIÓN @Dao MARCA ESTA INTERFAZ COMO UN OBJETO DE ACCESO A DATOS (DAO) PARA ROOM.
interface ProductoDao {

    @Query("SELECT * FROM productos")
    // USAMOS LiveData PARA QUE OTROS COMPONENTES DE LA APLICACION PUEDAN OBSERVAR LA LISTA Y SABER SU CONTENIDo
    fun getAllProductos(): LiveData<List<Producto>>

    // ESTA FUNCIÓN SUSPENDIDA OBTENDRÁ TODA LA LISTA DE OBJETOS Producto DE LA BASE DE DATOS.
    @Query("SELECT DISTINCT supermercado FROM productos ORDER BY supermercado ASC")
    fun getAllSupermercados(): Flow<List<String>>

    // "suspend" INDICA QUE ESTA FUNCIÓN DEBE SER LLAMADA DESDE UNA CORRUTINA.
    @Insert
    suspend fun insertarProducto(producto: Producto)

    @Delete
    suspend fun borrarProducto(producto: Producto)

    @Update
    suspend fun actualizarProducto(producto: Producto)
}