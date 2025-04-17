package com.example.asociacionpedrajeradepenas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import com.example.asociacionpedrajeradepenas.databinding.ActivityCrearEventoBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp
import java.util.UUID

class CrearEventoActivity : BaseActivity() {

    private lateinit var binding: ActivityCrearEventoBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseFirestore
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        storage = FirebaseStorage.getInstance()
        database = FirebaseFirestore.getInstance()

        // Configurar Toolbar reutilizando la lógica del BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)

        binding.btnSubirFoto.setOnClickListener {
            seleccionarImagen()
        }

        binding.btnCrearEvento.setOnClickListener {
            val nombre = binding.etNombreEvento.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val ubicacion = binding.etUbicacion.text.toString().trim()

            val fecha = binding.etFecha.text.toString().trim()
            val hora = binding.etHora.text.toString().trim()
            val fechaHoraString = "$fecha $hora"

            if (nombre.isNotEmpty() && ubicacion.isNotEmpty() && fecha.isNotEmpty() && hora.isNotEmpty() && imageUri != null) {
                subirImagenYGuardarEvento(nombre, descripcion, ubicacion, fechaHoraString)

                // Limpiar campos después de crear
                binding.etNombreEvento.setText("")
                binding.etDescripcion.setText("")
                binding.etUbicacion.setText("")
                binding.etFotoEvento.setText("")
                binding.etFecha.setText("")
                binding.etHora.setText("")
            } else {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let {
                val nombreArchivo = obtenerNombreArchivo(it)
                binding.etFotoEvento.setText(nombreArchivo)
            }
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "desconocido.jpg"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) nombre = it.getString(index)
            }
        }
        return nombre
    }

    private fun subirImagenYGuardarEvento(nombre: String, descripcion: String, ubicacion: String, fechaHora: String) {
        val storageRef = storage.reference.child("imagenes/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    guardarEventoEnFirestore(nombre, descripcion, ubicacion, fechaHora, imageUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarEventoEnFirestore(nombre: String, descripcion: String, ubicacion: String, fechaHora: String, imageUrl: String) {
        val idEvento = database.collection("Eventos").document().id

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaDate: Date = formatoFecha.parse(fechaHora)!!
        val fechaFirestore = Timestamp(fechaDate)


        val evento = hashMapOf(
            "idEvento" to idEvento,
            "nombre" to nombre,
            "descripcion" to descripcion.takeIf { it.isNotBlank() },
            "ubicacion" to ubicacion,
            "imagen" to imageUrl,
            "fecha_hora" to fechaFirestore
        )


        database.collection("Eventos").document(idEvento).set(evento)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento creado con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear el evento", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_CODE_IMAGE = 1
    }
}
