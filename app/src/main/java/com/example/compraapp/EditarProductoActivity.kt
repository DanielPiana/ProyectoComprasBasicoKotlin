package com.example.compraapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.compraapp.database.Producto
import com.example.compraapp.databinding.ActivityEditarProductoBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditarProductoActivity : AppCompatActivity() {

    // VARIABLE PARA EL VIEW BINDING
    private lateinit var binding: ActivityEditarProductoBinding

    // VARIABLE PARA EL VIEW MODEL DE PRODUCTOS
    private lateinit var productoViewModel: ProductoViewModel

    // VARIABLE PARA ALMACENAR EL PRODUCTO QUE SE VA A EDITAR
    private var productoAEditar: Producto? = null

    // CONSTANTE PARA EL CÓDIGO DE SOLICITUD DE LA INTENT DE LA CÁMARA
    private val REQUEST_IMAGE_CAPTURE = 1

    // VARIABLE PARA ALMACENAR LA URI DE LA FOTO ACTUALMENTE CAPTURADA O EXISTENTE
    private var currentPhotoUri: Uri? = null

    // VARIABLE PARA ALMACENAR LA RUTA ABSOLUTA DEL ARCHIVO DE LA FOTO ACTUALMENTE CAPTURADA
    private var currentPhotoPath: String? = null

    // VARIABLE PARA EL ADAPTADOR DEL SPINNER
    private lateinit var supermercadoAdapter: ArrayAdapter<String>

    // VARIABLE PARA ALMACENAR LA LISTA DE SUPERMERCADOS
    private var listaSupermercados: List<String> = emptyList()

    // MÉTODO LLAMADO CUANDO SE CREA LA ACTIVIDAD
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // INFLA EL LAYOUT DE LA ACTIVIDAD DE EDICIÓN UTILIZANDO VIEW BINDING
        binding = ActivityEditarProductoBinding.inflate(layoutInflater)
        // ESTABLECE LA VISTA DE CONTENIDO DE LA ACTIVIDAD
        setContentView(binding.root)

        // OBTIENE UNA INSTANCIA DEL VIEW MODEL DE PRODUCTOS
        productoViewModel = ViewModelProvider(this).get(ProductoViewModel::class.java)

        // OBTIENE EL OBJETO Producto PASADO A TRAVÉS DEL INTENT
        productoAEditar = intent.getParcelableExtra("producto")

        // INICIALIZAMOS LA LISTA DE ITEMS QUE VA A TENER EL SPINNER
        supermercadoAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf("Nuevo")
        )
        binding.spSupermercado.adapter = supermercadoAdapter

        // CONFIGURACION DEL LISTENER DEL SPINNER
        binding.spSupermercado.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                // MÉTODO LLAMADO CUANDO SE SELECCIONA UN ITEM DEL SPINNER
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // OBTENEMOS EL ITEM SELECCIONADO COMO String
                    val selectedItem = parent?.getItemAtPosition(position) as? String
                    // SI SE HA SELECCIONADO Nuevo, HACEMOS VISIBLE EL CAMPO PARA ESCRIBIR EL NUEVO SUPERMERCADO
                    if (selectedItem == "Nuevo") {
                        binding.tilNuevoSupermercado.visibility = View.VISIBLE
                    } else {
                        binding.tilNuevoSupermercado.visibility = View.GONE
                    }
                }

                // MÉTODO LLAMADO CUANDO NO SE SELECCIONA NADA
                override fun onNothingSelected(parent: AdapterView<*>?) {
                   // LO PONEMOS POR OBLIGACION
                }
            }

        // Observa la lista de supermercados
        productoViewModel.supermercadosList.observe(this) { supermercados ->
            // Actualiza el adaptador con la nueva lista
            val supermercadosConNuevo = supermercados.toMutableList()
            supermercadosConNuevo.add("Nuevo")
            supermercadoAdapter.clear()
            supermercadoAdapter.addAll(supermercadosConNuevo)
            supermercadoAdapter.notifyDataSetChanged()
            listaSupermercados = supermercados // Guardar la lista de supermercados

            // Si ya tenemos el producto a editar, intenta seleccionar el supermercado
            productoAEditar?.let { producto ->
                seleccionarSupermercadoEnSpinner(producto.supermercado)
            }
        }

        // LLAMA A LA FUNCION PARA INICIALIZAR LA UI CON LOS DATOS DEL PRODUCTO
        productoAEditar?.let { producto ->
            inicializarUIConProducto(producto)
        }

        // LISTENER PARA EL BOTON DE HACER UNA FOTO
        binding.btnHacerFoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // LISTENER PARA EL BOTON DE GUARDAR PRODUCTO
        binding.btnGuardarProducto.setOnClickListener {
            guardarProductoEditado()
        }
    }

    private fun inicializarUIConProducto(producto: Producto) {
        // ESTABLECE EL TEXTO DEL NOMBRE DEL PRODUCTO EN EL CAMPO DE EDICIÓN
        binding.etNombreProductoCreare.setText(producto.nombre)
        // ESTABLECE EL TEXTO DEL PRECIO DEL PRODUCTO EN EL CAMPO DE EDICIÓN
        binding.etPrecio.setText(producto.precio.toString())

        // SI EL PRODUCTO TIENE UNA URI DE FOTO GUARDADA
        producto.fotoUri?.let { uriString ->
            // PARSEA LA CADENA DE LA URI A UN OBJETO URI
            currentPhotoUri = Uri.parse(uriString)
            // UTILIZA GLIDE PARA CARGAR LA IMAGEN EN EL IMAGEVIEW
            Glide.with(this)
                .load(currentPhotoUri)
                .into(binding.ivFotoProductoCrear)
        }

        seleccionarSupermercadoEnSpinner(producto.supermercado)
    }

    // METODO PARA SETEAR EL SUPERMERCADO DEL PRODUCTO EN EL SPINNER
    private fun seleccionarSupermercadoEnSpinner(supermercado: String) {
        // OBTENEMOS EL INDICE DEL SUPERMERCADO EN EL SPINNER
        val index = supermercadoAdapter.getPosition(supermercado)
        if (index != -1) { // SI EXISTE
            binding.spSupermercado.setSelection(index)
        } else { // SI NO EXISTE
            val nuevoIndex = supermercadoAdapter.getPosition("Nuevo")
            if (nuevoIndex != -1) {
                binding.spSupermercado.setSelection(nuevoIndex)
                binding.tilNuevoSupermercado.visibility = View.VISIBLE
                binding.etNuevoSupermercado.setText(supermercado)
            }
        }
    }

    // FUNCIÓN PARA HACER EL INTENT DE LA FOTO
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.let {
                // CREA EL ARCHIVO DONDE SE GUARDARÁ LA FOTO
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("EditarProductoActivity", "Error creating image file", ex)
                    null
                }
                // SI EL ARCHIVO SE CREÓ EXITOSAMENTE
                photoFile?.also {
                    // OBTIENE UNA URI PARA EL ARCHIVO UTILIZANDO FILEPROVIDER
                    val photoURI: Uri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        "com.example.compraapp.fileprovider",
                        it
                    )
                    // EXTRA: PASA LA URI DE SALIDA A LA INTENT DE LA CÁMARA PARA QUE GUARDE LA FOTO AHÍ
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                    // INICIA LA ACTIVIDAD DE LA CÁMARA ESPERANDO UN RESULTADO
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                    // BORRA LA FOTO ANTERIOR SI EXISTE Y ES DIFERENTE DE LA NUEVA
                    val fotoUriAnterior = productoAEditar?.fotoUri?.let { Uri.parse(it) }
                    if (fotoUriAnterior != null && fotoUriAnterior != photoURI) {
                        try {
                            contentResolver.delete(fotoUriAnterior, null, null)
                            Log.d(
                                "EditarProductoActivity",
                                "Foto anterior borrada: $fotoUriAnterior"
                            )
                        } catch (e: SecurityException) {
                            Log.e("EditarProductoActivity", "Error al borrar la foto anterior: $e")
                        }
                    }

                    // GUARDA LA URI DE LA FOTO ACTUAL
                    currentPhotoUri = photoURI
                    // GUARDA LA RUTA ABSOLUTA DEL ARCHIVO DE LA FOTO ACTUAL
                    currentPhotoPath = it.absolutePath
                }
            }
        }
    }

    // FUNCIÓN PARA CREAR UN ARCHIVO TEMPORAL PARA LA IMAGEN
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // CREA UN NOMBRE DE ARCHIVO ÚNICO BASADO EN LA MARCA DE TIEMPO
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // OBTIENE EL DIRECTORIO DE ALMACENAMIENTO PARA LAS IMÁGENES PRIVADAS DE LA APLICACIÓN
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // LOG PARA DEPURACIÓN: MUESTRA LA RUTA DEL DIRECTORIO DE ALMACENAMIENTO
        Log.d("EditarProductoActivity", "Storage directory: $storageDir")
        // CREA UN ARCHIVO TEMPORAL CON UN PREFIJO, SUFIJO Y DIRECTORIO ESPECIFICADOS
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // GUARDA LA RUTA ABSOLUTA DEL ARCHIVO CREADO
            currentPhotoPath = absolutePath
        }
    }

    // MÉTODO LLAMADO CUANDO UNA ACTIVIDAD INICIADA CON startActivityForResult() TERMINA
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // SI EL CÓDIGO DE SOLICITUD COINCIDE CON EL DE LA CÁMARA Y EL RESULTADO ES OK
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // SI LA URI DE LA FOTO ACTUAL NO ES NULA
            currentPhotoUri?.let { uri ->
                // UTILIZA GLIDE PARA CARGAR LA IMAGEN DESDE LA URI EN EL IMAGEVIEW
                Glide.with(binding.ivFotoProductoCrear)
                    .load(uri)
                    .into(binding.ivFotoProductoCrear)
            } ?: run {
                // LOG DE ADVERTENCIA SI currentPhotoUri ES NULO DESPUÉS DE TOMAR LA FOTO
                Log.w("EditarProductoActivity", "currentPhotoUri is null after taking photo")
            }
        }
    }

    // FUNCIÓN PARA GUARDAR LOS CAMBIOS DEL PRODUCTO EDITADO
    private fun guardarProductoEditado() {
        // OBTIENE EL TEXTO INGRESADO EN EL CAMPO DE NOMBRE DEL PRODUCTO Y ELIMINA ESPACIOS EN BLANCO
        val nombre = binding.etNombreProductoCreare.text.toString().trim()
        // OBTIENE EL TEXTO INGRESADO EN EL CAMPO DE PRECIO Y ELIMINA ESPACIOS EN BLANCO
        val precioStr = binding.etPrecio.text.toString().trim()
        val supermercado: String
        // SI EL CAMPO PARA INGRESAR UN NUEVO SUPERMERCADO ES VISIBLE
        if (binding.tilNuevoSupermercado.visibility == View.VISIBLE) {
            // OBTIENE EL TEXTO DEL NUEVO SUPERMERCADO Y ELIMINA ESPACIOS EN BLANCO
            val nuevoSupermercado = binding.etNuevoSupermercado.text.toString().trim()
            // SI EL CAMPO DE NUEVO SUPERMERCADO ESTÁ VACÍO
            if (nuevoSupermercado.isEmpty()) {
                // MUESTRA UN TOAST PIDIENDO INGRESAR EL NUEVO SUPERMERCADO Y SALE
                Toast.makeText(this, "Por favor, ingresa el nuevo supermercado", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            // VERIFICAR SI EL SUPERMERCADO YA EXISTE
            if (listaSupermercados.contains(nuevoSupermercado)) {
                Toast.makeText(
                    this,
                    "El supermercado '$nuevoSupermercado' ya existe. Por favor, selecciónalo de la lista.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            supermercado = nuevoSupermercado
        } else {
            // SI EL CAMPO DE NUEVO SUPERMERCADO NO ESTÁ VISIBLE, SE SELECCIONÓ DEL SPINNER
            supermercado = binding.spSupermercado.selectedItem as? String ?: ""
            // SI NO SE SELECCIONÓ UN SUPERMERCADO VÁLIDO
            if (supermercado.isEmpty() || supermercado == "Nuevo") {
                // MUESTRA UN TOAST PIDIENDO SELECCIONAR UN SUPERMERCADO Y SALE
                Toast.makeText(this, "Por favor, selecciona un supermercado", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }

        // SI EL NOMBRE O EL PRECIO ESTÁN VACÍOS
        if (nombre.isEmpty() || precioStr.isEmpty()) {
            // MUESTRA UN TOAST PIDIENDO COMPLETAR LOS CAMPOS Y SALE
            Toast.makeText(this, "Por favor, completa el nombre y el precio", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // INTENTA CONVERTIR EL PRECIO A DOUBLE
        val precio = try {
            precioStr.toDouble()
        } catch (e: NumberFormatException) {
            // SI EL PRECIO NO ES UN NÚMERO VÁLIDO, MUESTRA UN TOAST Y SALE
            Toast.makeText(this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        // OBTIENE LA NUEVA URI DE LA FOTO O MANTIENE LA ANTERIOR SI NO SE TOMÓ UNA NUEVA
        val fotoUriString = currentPhotoUri?.toString() ?: productoAEditar?.fotoUri

        // SI SE OBTUVO EL PRODUCTO ORIGINAL PARA EDITAR
        productoAEditar?.let { productoOriginal ->
            // CREA UN NUEVO OBJETO Producto CON LOS DATOS EDITADOS, MANTENIENDO EL ID ORIGINAL
            val productoEditado = Producto(
                id = productoOriginal.id, // ¡MANTENER EL ID ORIGINAL!
                nombre = nombre,
                supermercado = supermercado,
                precio = precio,
                fotoUri = fotoUriString
            )
            // LLAMA A LA FUNCIÓN DEL VIEW MODEL PARA ACTUALIZAR EL PRODUCTO EN LA BASE DE DATOS
            productoViewModel.actualizarProducto(productoEditado)
            // MUESTRA UN TOAST INDICANDO QUE EL PRODUCTO SE HA ACTUALIZADO
            Toast.makeText(this, "${productoEditado.nombre} actualizado", Toast.LENGTH_SHORT).show()
            // FINALIZA LA ACTIVIDAD Y VUELVE A LA ANTERIOR
            finish()
        } ?: run {
            // SI NO SE PUDO OBTENER LA INFORMACIÓN DEL PRODUCTO A EDITAR, MUESTRA UN TOAST DE ERROR
            Toast.makeText(
                this,
                "Error: No se pudo obtener la información del producto a editar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
