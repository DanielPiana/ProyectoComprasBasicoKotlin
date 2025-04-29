package com.example.compraapp.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.compraapp.database.Producto
import com.example.compraapp.databinding.ItemProductoBinding

class ProductoViewHolder(
    // RECIBE LA INSTANCIA DE VIEW BINDING PARA EL LAYOUT DEL ITEM
    private val binding: ItemProductoBinding,
    // RECIBE LA INSTANCIA DEL LISTENER PARA EL EVENTO DE ELIMINAR PRODUCTO
    private val eliminarListener: ProductoAdapter.OnProductoEliminarClickListener,
    // RECIBE LA INSTANCIA DEL LISTENER PARA EL EVENTO DE EDITAR PRODUCTO
    private val editarListener : ProductoAdapter.OnProductoEditarClickListener
    // LLAMA AL CONSTRUCTOR DE LA CLASE PADRE (RecyclerView.ViewHolder) PASANDO LA VISTA RAIZ DEL LAYOUT DEL ITEM
) : RecyclerView.ViewHolder(binding.root) {

    // FUNCIÓN PARA ASIGNAR LOS DATOS DEL PRODUCTO A LAS VISTAS DEL ITEM
    fun render(productoModel: Producto) {
        // ESTABLECE EL TEXTO DEL NOMBRE DEL PRODUCTO EN EL TEXTVIEW CORRESPONDIENTE
        binding.tvNombreProducto.text = productoModel.nombre
        // ESTABLECE EL TEXTO DEL SUPERMERCADO EN EL TEXTVIEW CORRESPONDIENTE
        binding.tvSupermercado.text = productoModel.supermercado
        // ESTABLECE EL TEXTO DEL PRECIO (CONVERTIDO A STRING) EN EL TEXTVIEW CORRESPONDIENTE
        binding.tvPrecio.text = productoModel.precio.toString()
        // UTILIZA LA LIBRERÍA GLIDE PARA CARGAR LA IMAGEN DESDE LA URI EN EL IMAGEVIEW CORRESPONDIENTE
        Glide.with(binding.ivFotoProducto.context)
            .load(productoModel.fotoUri)
            .into(binding.ivFotoProducto)

        // ESTABLECE UN OnClickListener PARA EL BOTÓN DE BORRAR PRODUCTO
        binding.ibBorrarProducto.setOnClickListener {
            // CUANDO SE HACE CLICK, LLAMA AL MÉTODO onProductoEliminarClicked DEL LISTENER, PASANDO EL PRODUCTO ACTUAL
            eliminarListener.onProductoEliminarClicked(productoModel)
        }

        // ESTABLECE UN OnClickListener PARA EL BOTÓN DE EDITAR PRODUCTO
        binding.ibEditarProducto.setOnClickListener {
            // CUANDO SE HACE CLICK, LLAMA AL MÉTODO onProductoEditarClicked DEL LISTENER DE EDICIÓN, PASANDO EL PRODUCTO ACTUAL
            editarListener.onProductoEditarClicked(productoModel)
        }
    }
}