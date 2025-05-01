package com.example.compraapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.compraapp.adapter.ProductoAdapter
import com.example.compraapp.database.Producto
import com.example.compraapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ProductoAdapter.OnProductoEliminarClickListener, ProductoAdapter.OnProductoEditarClickListener { // IMPLEMENTAMOS LA INTERFAZ PARA EL BORRADO DE LOS PRODUCTOS
    /*
    AppDatabase CLASE PARA LA BASE DE DATOS
    Producto DATA CLASS (CLASE OBJETO)
    ProductoDao CLASE PARA INTERACTUAR CON LA BASE DE DATOS
    ProductoViewModel ACTUA DE INTERMEDIARIO ENTRE LOS ACTIVITY/FRAGMENT Y LA BASE DE DATOS, LE PIDE A ProductoDao LOS DATOS QUE NECESITA EL ACTIVITY/FRAGMENT
    ProductoAdapter-> LA CLASE QUE SE ENCARGA DE COGER LA INFORMACION Y METERLA EN EL RECYCLERVIEW, ASI COMO ESTABLECER ACCIONES CUANDO DAS CLICK A UN COMPONENTE
    ProductoViewHolder -> LA CLASE QUE SE ENCARGA DE LA VISTA DE UN UNICO ITEM DENTRO DE LA LISTA (SIRVE PARA TODOS)
    */

    private lateinit var binding: ActivityMainBinding
    private lateinit var productoViewModel: ProductoViewModel
    private lateinit var productoAdapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // CREAMOS EL BINDING PARA ACCEDER A LOS WIDGETS POR SU ID SIN TENER QUE USAR R
        setContentView(binding.root)
        initRecyclerView()
        observeProductos()


        binding.fabCrearProducto.setOnClickListener {
            val intent = Intent(this,CrearProductoActivity::class.java)
            startActivity(intent)
        }

        // LISTENER DEL BOTON PARA ORDENAR POR SUPERMERCADOS
        binding.btnOrdenarPorSupermercado.setOnClickListener {
            mostrarListaDeSupermercados()
        }
    }

    private fun initRecyclerView() {
        // CREA UNA INSTANCIA DE LinearLayoutManager. ESTE OBJETO SE ENCARGA DE ORGANIZAR LOS ITEMS DEL RECYCLERVIEW EN UNA LISTA VERTICAL U HORIZONTAL.
        val manager = LinearLayoutManager(this)

        // CREA UNA INSTANCIA DE DividerItemDecoration. ESTE OBJETO SE UTILIZA PARA DIBUJAR LÍNEAS DE DIVISIÓN ENTRE LOS ITEMS DEL RECYCLERVIEW.
        val decoration = DividerItemDecoration(this, manager.orientation)

        // ASIGNA EL LinearLayoutManager AL RECYCLERVIEW
        binding.recyclerProducto.layoutManager = manager

        // INICIALIZA LA VARIABLE productoAdapter CON UNA NUEVA INSTANCIA DE ProductoAdapter.
        // LE PASA UNA LISTA VACÍA COMO DATOS INICIALES Y 'this' (LA ACTIVIDAD ACTUAL) COMO LOS LISTENERS PARA ELIMINAR Y EDITAR PRODUCTOS.
        productoAdapter = ProductoAdapter(emptyList(), this, this)

        // ASIGNA LA INSTANCIA DE ProductoAdapter AL RECYCLERVIEW. ESTO CONECTA LOS DATOS Y LA LÓGICA DE PRESENTACIÓN CON LA VISTA DEL RECYCLERVIEW.
        binding.recyclerProducto.adapter = productoAdapter

        // OPCIONAL: AÑADE LA DECORACIÓN (LAS LÍNEAS DIVISORIAS) AL RECYCLERVIEW PARA SEPARAR VISUALMENTE LOS ITEMS.
        binding.recyclerProducto.addItemDecoration(decoration)
    }

    private fun observeProductos() {
        // OBTENEMOS UNA INSTANCIA DE ViewModel
        productoViewModel = ViewModelProvider(this).get(ProductoViewModel::class.java)

        // OBSERVAMOS CAMBIOS EN LA LISTA DE PRODUCTOS CON LiveData
        productoViewModel.allProductos.observe(this) { productos ->
            //CUANDO LA LISTA DE PRODUCTOS CAMBIA, ACTUALIZAMOS LA LISTA EN EL ADAPTER
            productoAdapter.actualizarLista(productos)
        }
    }

    // METODO DE BORRAR PRODUCTO
    override fun onProductoEliminarClicked(producto: Producto) {
        // LLAMAMOS A LA FUNCION DE BORRADO DEL ViewModel
        productoViewModel.borrarProducto(producto)

        Toast.makeText(this, "${producto.nombre} eliminado", Toast.LENGTH_SHORT).show()

    }

    override fun onProductoEditarClicked(producto: Producto) {
        // COMO QUEREMOS MOSTRAR UNA ACTIVITY NUEVA PARA PEDIR LOS NUEVOS DATOS, TENEMOS QUE HACER UN Intent
        val intent = Intent(this, EditarProductoActivity::class.java)
        // PONEMOS EL Producto A ACTUALIZAR EN EL EXTRA DEL Intent, PARA MANDARSELO A LA ACTIVITY EditarProductoActivity
        // SI NO HICIESE PRODUCTO Parcelable, TENDRIA QUE PASAR LOS ATRIBUTOS UNO A UNO.
        intent.putExtra("producto", producto)
        startActivity(intent)
    }

    // FUNCION PARA MOSTRAR LA LISTA DE LOS SUPERMERCADOS
    private fun mostrarListaDeSupermercados() {
        productoViewModel.supermercadosList.observe(this) { supermercados ->
            val supermercadosConTodos = supermercados.toMutableList()
            supermercadosConTodos.add(0, "Todos") // Añadimos la opción "Todos" al inicio
            val supermercadosArray = supermercadosConTodos.toTypedArray()

            // CONVERTIMOS A TypedArray PORQUE AlertDialog.Builder.setItems LO REQUIERE
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Supermercado")
                .setItems(supermercadosArray) { dialog, selectedIndex ->
                    val supermercadoSeleccionado = supermercadosArray[selectedIndex]
                    ordenarProductosPorSupermercado(supermercadoSeleccionado)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    // AÑADIMOS ESTA FUNCIÓN PARA ORDENAR LOS PRODUCTOS POR SUPERMERCADO
    private fun ordenarProductosPorSupermercado(supermercado: String) {
        // TENEMOS QUE ESCUCHAR LA LISTA DE PRODUCTOS PARA CUANDO SE AÑADE UNO NUEVO
        productoViewModel.allProductos.observe(this) { productos ->
            // SI HA SELECCIONADO "Todos" DEVOLVEMOS LA LISTA DE SERIE SIN ORGANIZARLA
            val productosOrdenados = if (supermercado == "Todos") {
                productos
            } else {
                // SI HA SELECCIONADO UN SUPERMERCADO, LO ORDENAMOS USANDOLO (PARAMETRO supermercado)
                productos.sortedWith(compareBy {
                    // EL VALOR 0 INDICA QUE SE MUESTRA PRIMERO
                    if (it.supermercado == supermercado) 0 else 1
                })
            }
            // ACTUALIZAMOS LA LISTA DE PRODUCTOS EN EL ADAPTADOR PARA QUE SE MUESTREN ORDENADOS
            productoAdapter.actualizarLista(productosOrdenados)
        }
    }
}