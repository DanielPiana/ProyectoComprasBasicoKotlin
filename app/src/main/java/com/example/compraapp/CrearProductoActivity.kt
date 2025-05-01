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
import com.example.compraapp.databinding.ActivityProductoNuevoBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrearProductoActivity : AppCompatActivity() {
    // VARIABLE PARA EL VIEW BINDING
    private lateinit var binding: ActivityProductoNuevoBinding

    // VARIABLE PARA EL VIEW MODEL DE PRODUCTOS
    private lateinit var productoViewModel: ProductoViewModel

    // CONSTANTE PARA EL CÓDIGO DE SOLICITUD DE LA INTENT DE LA CÁMARA
    private val REQUEST_IMAGE_CAPTURE = 1

    // VARIABLE PARA ALMACENAR LA URI DE LA FOTO ACTUALMENTE CAPTURADA
    private var currentPhotoUri: Uri? = null

    // VARIABLE PARA ALMACENAR LA RUTA ABSOLUTA DEL ARCHIVO DE LA FOTO ACTUALMENTE CAPTURADAº
    private var currentPhotoPath: String? = null

    // VARIABLE PARA ALMACENAR LA LISTA DE SUPERMERCADOS
    private var listaSupermercados: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoNuevoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // INSTANCIA DEL VIEW MODEL DE PRODUCTOS
        productoViewModel = ViewModelProvider(this).get(ProductoViewModel::class.java)

        // OBSERVA EL LiveData QUE CONTIENE LA LISTA DE SUPERMERCADOS
        productoViewModel.supermercadosList.observe(this) { supermercados ->

            // LOG DE DEPURACION, IMPRIME LOS SUPERMERCADOS
            Log.d(
                "CrearProductoActivity",
                "Supermercados recibidos del ViewModel (LiveData): $supermercados"
            )

            // CREAMOS UNA COPIA MUTABLE DE LA LISTA DE SUPERMERCADOS Y LE AÑADIMOS LA OPCION Nuevo
            val supermercadosConNuevo = supermercados.toMutableList()
            supermercadosConNuevo.add("Nuevo")

            // GUARDAMOS LA LISTA PARA USARLA DESPUES
            listaSupermercados = supermercados

            // CREA UN ADAPTADOR PARA EL SPINNER CON LA LISTA DE SUPERMERCADOS Y LO ASIGNAMOS AL Spinner
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                supermercadosConNuevo
            )
            binding.spSupermercado.adapter = adapter

            // ESTABLECE EL LISTENER PARA LOS EVENTOS DE SELECCIÓN DEL SPINNER
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
        }
        // LISTENER PARA EL BOTON DE HACER UNA FOTO
        binding.btnHacerFoto.setOnClickListener {
            dispatchTakePictureIntent()
        }
        // LISTENER PARA EL BOTON DE GUARDAR PRODUCTO
        binding.btnGuardarProducto.setOnClickListener {
            guardarNuevoProducto()
        }
    }

    // FUNCION PARA HACER EL INTENT DE LA FOTO
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.let {
                // CREA EL ARCHIVO DONDE SE GUARDARÁ LA FOTO
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // LOG DE DEPURACIÓN
                    Log.e("CrearProductoActivity", "Error creating image file", ex)
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
        Log.d("CrearProductoActivity", "Storage directory: $storageDir")
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
                Log.w("CrearProductoActivity", "currentPhotoUri is null after taking photo")
            }

            // VERIFICA SI EL ARCHIVO EXISTE DESPUÉS DE ON ACTIVITY RESULT
            currentPhotoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    Log.d("CrearProductoActivity", "¡El archivo EXISTE en: $path!")
                } else {
                    Log.w("CrearProductoActivity", "¡El archivo NO EXISTE en: $path!")
                }
            }
        }
    }

    // FUNCIÓN PARA GUARDAR UN NUEVO PRODUCTO
    private fun guardarNuevoProducto() {
        // OBTIENE EL TEXTO INGRESADO EN EL CAMPO DE NOMBRE DEL PRODUCTO Y ELIMINA ESPACIOS EN BLANCO AL INICIO Y AL FINAL
        val nombre = binding.etNombreProductoCreare.text.toString().trim()
        // OBTIENE EL TEXTO INGRESADO EN EL CAMPO DE PRECIO Y ELIMINA ESPACIOS EN BLANCO
        val precioStr = binding.etPrecio.text.toString().trim()
        val supermercado: String

        // SI EL CAMPO PARA INGRESAR UN NUEVO SUPERMERCADO ES VISIBLE
        if (binding.tilNuevoSupermercado.visibility == View.VISIBLE) {
            // OBTIENE EL TEXTO INGRESADO EN EL CAMPO DE NUEVO SUPERMERCADO Y ELIMINA ESPACIOS EN BLANCO
            val nuevoSupermercado = binding.etNuevoSupermercado.text.toString().trim()
            // SI EL CAMPO DE NUEVO SUPERMERCADO ESTÁ VACÍO
            if (nuevoSupermercado.isEmpty()) {
                // MUESTRA UN TOAST PIDIENDO AL USUARIO QUE INGRESE EL NUEVO SUPERMERCADO Y SALE DE LA FUNCIÓN
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
            // SI EL CAMPO DE NUEVO SUPERMERCADO NO ESTÁ VISIBLE, SE HA SELECCIONADO UN SUPERMERCADO DEL SPINNER
            supermercado = binding.spSupermercado.selectedItem as? String ?: ""
            // SI NO SE HA SELECCIONADO UN SUPERMERCADO O SE SELECCIONÓ "Nuevo" (PERO EL CAMPO NO ESTÁ VISIBLE POR ERROR)
            if (supermercado.isEmpty() || supermercado == "Nuevo") {
                // MUESTRA UN TOAST PIDIENDO AL USUARIO QUE SELECCIONE UN SUPERMERCADO Y SALE DE LA FUNCIÓN
                Toast.makeText(this, "Por favor, selecciona un supermercado", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        // SI EL CAMPO DE NOMBRE O PRECIO ESTÁ VACÍO
        if (nombre.isEmpty() || precioStr.isEmpty()) {
            // MUESTRA UN TOAST PIDIENDO AL USUARIO QUE COMPLETE EL NOMBRE Y EL PRECIO Y SALE DE LA FUNCIÓN
            Toast.makeText(this, "Por favor, completa el nombre y el precio", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // INTENTA CONVERTIR LA CADENA DE PRECIO A UN DOUBLE
        val precio = try {
            precioStr.toDouble()
        } catch (e: NumberFormatException) {
            // SI LA CONVERSIÓN FALLA, MUESTRA UN TOAST INDICANDO QUE EL PRECIO DEBE SER UN NÚMERO VÁLIDO Y SALE DE LA FUNCIÓN
            Toast.makeText(this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        // OBTIENE LA URI DE LA FOTO ACTUAL COMO UNA CADENA (PUEDE SER NULA)
        val fotoUriString = currentPhotoUri?.toString()

        // CREA UN NUEVO OBJETO Producto CON LOS DATOS OBTENIDOS
        val nuevoProducto = Producto(
            nombre = nombre,
            supermercado = supermercado,
            precio = precio,
            fotoUri = fotoUriString
        )

        // LLAMA A LA FUNCIÓN DEL VIEW MODEL PARA INSERTAR EL NUEVO PRODUCTO EN LA BASE DE DATOS
        productoViewModel.insertarProducto(nuevoProducto)
        // MUESTRA UN TOAST INDICANDO QUE EL PRODUCTO SE HA GUARDADO
        Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
        // FINALIZA LA ACTIVIDAD Y VUELVE A LA ACTIVIDAD ANTERIOR
        finish()
    }
}
/*FLUJO DE GUARDAR FOTO

*1- El usuario hace clic en "Hacer Foto".

2- dispatchTakePictureIntent() crea un archivo temporal para la foto y obtiene su Uri (usando FileProvider).
Esta Uri se guarda en currentPhotoUri y se pasa a la Intent de la cámara para que la foto en alta resolución se guarde en ese lugar.

3- La aplicación de la cámara toma la foto y la guarda en la ubicación especificada por la Uri.

4- onActivityResult() se llama cuando la cámara devuelve el control a nuestra aplicación. Si la operación fue exitosa, tenemos la Uri de la foto guardada en currentPhotoUri.

5- Cuando el usuario hace clic en "Guardar Producto", la función guardarNuevoProducto() toma el valor de currentPhotoUri (que ahora contiene la URI de la foto guardada)
y lo asigna al atributo fotoUri del nuevo objeto Producto que se va a insertar en la base de datos a través de la ProductoViewModel.

* */