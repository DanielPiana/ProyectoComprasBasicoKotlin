package com.example.compraapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.compraapp.database.Producto
import com.example.compraapp.databinding.ItemProductoBinding

class ProductoAdapter(
    private var productosList: List<Producto>,
    private val eliminarListener: OnProductoEliminarClickListener,
    private val editarListener: OnProductoEditarClickListener): RecyclerView.Adapter<ProductoViewHolder>() {


    // METODO PARA ACTUALIZAR LA LISTA DE PRODUCTOS CUANDO SE AÑADE, SE BORRA O SE MODIFICA ALGUNO.
    fun actualizarLista(nuevaLista: List<Producto>) {
        productosList = nuevaLista
        notifyDataSetChanged() // NOTIFICA AL RecyclerView QUE LOS DATOS HAN CAMBIADO
    }

    // METODO PARA CREAR Y DEVOLVER UNA NUEVA INSTANCIA DE ViewHolder
    // SE ENCARGA DE CREAR LA ESTRUCTURA VISUAL DE CADA ITEM (INFLADO POR EL LAYOUT) Y CREAR EL CONTENEDOR PARA ESAS VISTAS, PROPORCIONANDO HERRAMIENTAS
    // NECESARIAS PARA INTERACTUAR CON EL RESTO DEL CODIGO (LOS LISTENERS)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductoViewHolder(ItemProductoBinding.inflate(layoutInflater, parent, false),eliminarListener,editarListener)
    }
    // METODO PARA OBTENER EL NUMERO DE PRODUCTOS
    override fun getItemCount(): Int {
        return productosList.size
    }

    // METODO QUE SE ENCARGA DE ENLAZAR LOS DATOS DE LA LISTA CON LA VISTA DE UN ITEM EN ESPECIFICO, QUE YA HA SIDO CREADO POR onCreateViewHolder
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val item = productosList[position]
        holder.render(item)
    }

    // METODO PARA ELIMINAR PRODUCTO, LLAMA AL METODO onProductoEliminarClicked ENVIANDOLE EL PRODUCTO A BORRAR
    interface OnProductoEliminarClickListener {
        fun onProductoEliminarClicked(producto: Producto)
    }

    // METODO PARA editar PRODUCTO, LLAMA AL METODO onProductoEditarClicked ENVIANDOLE EL PRODUCTO A EDITAR
    interface OnProductoEditarClickListener {
        fun onProductoEditarClicked(producto: Producto)
    }
}
