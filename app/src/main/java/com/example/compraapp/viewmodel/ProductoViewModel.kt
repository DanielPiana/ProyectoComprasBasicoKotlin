package com.example.compraapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.compraapp.database.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var productoDao: ProductoDao
    val allProductos: LiveData<List<Producto>> by lazy { productoDao.getAllProductos() }
    private val _supermercadosList = MutableLiveData<List<String>>()
    val supermercadosList: LiveData<List<String>> = _supermercadosList

    init {
        val database = AppDatabase.getDatabase(application)
        productoDao = database.productoDao()
        cargarSupermercados()
    }

    private fun cargarSupermercados() {
        viewModelScope.launch(Dispatchers.IO) {
            val supermercadosFlow: Flow<List<String>> = productoDao.getAllSupermercados()
            supermercadosFlow.first().let { lista ->
                _supermercadosList.postValue(lista) // Usar postValue para actualizar el LiveData desde un hilo de fondo
                Log.d("ProductoViewModel", "Supermercados cargados (LiveData): $lista")
            }
        }
    }

    fun insertarProducto(producto: Producto) = viewModelScope.launch(Dispatchers.IO) {
        productoDao.insertarProducto(producto)
    }

    fun borrarProducto(producto:Producto) = viewModelScope.launch(Dispatchers.IO) {
        productoDao.borrarProducto(producto)
    }

    fun actualizarProducto(producto: Producto) = viewModelScope.launch(Dispatchers.IO) {
        productoDao.actualizarProducto(producto)
        Log.d("ProductoViewModel", "Producto actualizado: ${producto.nombre} (ID: ${producto.id})")
    }
}