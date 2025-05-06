package com.example.compraapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.compraapp.database.Producto
import com.example.compraapp.databinding.ItemProductoBinding
import com.bumptech.glide.Glide
import com.example.compraapp.R
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class ProductoAdapter(
    private var productosList: List<Producto>,
    private val eliminarListener: OnProductoEliminarClickListener,
    private val editarListener: OnProductoEditarClickListener
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private var posicionesProductosCogidos = mutableSetOf<Int>()

    // METODO PARA ACTUALIZAR LA LISTA DE PRODUCTOS CUANDO SE AÑADE, SE BORRA O SE MODIFICA ALGUNO.
    fun actualizarLista(nuevaLista: List<Producto>) {
        productosList = nuevaLista
        notifyDataSetChanged()
    }

    // METODO PARA CREAR Y DEVOLVER UNA NUEVA INSTANCIA DE ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemProductoBinding.inflate(layoutInflater, parent, false)
        return ProductoViewHolder(binding, eliminarListener, editarListener)
    }

    // METODO PARA OBTENER EL NUMERO DE PRODUCTOS
    override fun getItemCount(): Int {
        return productosList.size
    }

    // METODO QUE SE ENCARGA DE ENLAZAR LOS DATOS DE LA LISTA CON LA VISTA DE UN ITEM EN ESPECIFICO, QUE YA HA SIDO CREADO POR onCreateViewHolder
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val item = productosList[position]
        holder.render(item)

        // OBTENEMOS LA REFERENCIA DEL CardView DEL ITEM Y ESTABLECEMOS OnClickListener
        holder.binding.root.setOnClickListener {
            // // CAMBIAMOS EL FINDO DEL CardView BASANDONOS EN SI ESTA "Cogido" O NO
            if (posicionesProductosCogidos.contains(position)) {
                holder.binding.root.background = null // RESETEAMOS EL FONDO
                posicionesProductosCogidos.remove(position)
            } else {
                holder.binding.root.setBackgroundResource(R.drawable.marcador_borde) // ESTABLECEMOS EL BORDE INDICANDO QUE ESTA COGIDO
                posicionesProductosCogidos.add(position)
            }
            notifyItemChanged(position) // NOTIFICAMOS QUE UN ITEM HA CAMBIADO PARA ACTUALIZARLO
        }

        if (posicionesProductosCogidos.contains(position)) {
            holder.binding.root.setBackgroundResource(R.drawable.marcador_borde)
        } else {
            holder.binding.root.background = null
        }
    }

    // METODO PARA ELIMINAR PRODUCTO, LLAMA AL METODO onProductoEliminarClicked ENVIANDOLE EL PRODUCTO A BORRAR
    interface OnProductoEliminarClickListener {
        fun onProductoEliminarClicked(producto: Producto)
    }

    // METODO PARA editar PRODUCTO, LLAMA AL METODO onProductoEditarClicked ENVIANDOLE EL PRODUCTO A EDITAR
    interface OnProductoEditarClickListener {
        fun onProductoEditarClicked(producto: Producto)
    }

    class ProductoViewHolder(
        val binding: ItemProductoBinding,
        private val eliminarListener: OnProductoEliminarClickListener,
        private val editarListener: OnProductoEditarClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun render(producto: Producto) {
            // ESTABLECE EL TEXTO DEL NOMBRE DEL PRODUCTO EN EL TEXTVIEW CORRESPONDIENTE
            binding.tvNombreProducto.text = producto.nombre
            // ESTABLECE EL TEXTO DEL SUPERMERCADO EN EL TEXTVIEW CORRESPONDIENTE
            binding.tvSupermercado.text = producto.supermercado
            // ESTABLECE EL TEXTO DEL PRECIO (CONVERTIDO A STRING) EN EL TEXTVIEW CORRESPONDIENTE
            binding.tvPrecio.text = producto.precio.toString()

            // CARGAMOS LA IMAGEN USANDO GLIDE
            if (producto.fotoUri != null) {
                // UTILIZA LA LIBRERÍA GLIDE PARA CARGAR LA IMAGEN DESDE LA URI EN EL IMAGEVIEW CORRESPONDIENTE
                Glide.with(binding.ivFotoProducto.context)
                    .load(producto.fotoUri)
                    .into(binding.ivFotoProducto)
            } else {
                Glide.with(binding.ivFotoProducto.context)
                    .load(R.drawable.ic_launcher_background)
                    .into(binding.ivFotoProducto)
            }

            // ESTABLECE UN OnClickListener PARA EL BOTÓN DE BORRAR PRODUCTO
            binding.ibBorrarProducto.setOnClickListener {
                // CUANDO SE HACE CLICK, LLAMA AL MÉTODO onProductoEliminarClicked DEL LISTENER, PASANDO EL PRODUCTO ACTUAL
                eliminarListener.onProductoEliminarClicked(producto)
            }

            // ESTABLECE UN OnClickListener PARA EL BOTÓN DE EDITAR PRODUCTO
            binding.ibEditarProducto.setOnClickListener {
                // CUANDO SE HACE CLICK, LLAMA AL MÉTODO onProductoEditarClicked DEL LISTENER DE EDICIÓN, PASANDO EL PRODUCTO ACTUAL

                editarListener.onProductoEditarClicked(producto)
            }
        }
    }
}
