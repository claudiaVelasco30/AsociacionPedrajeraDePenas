package com.example.asociacionpedrajeradepenas

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.asociacionpedrajeradepenas.databinding.FragmentCrearBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale
import java.util.UUID

class CrearFragment : Fragment() {

    private var _binding: FragmentCrearBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseFirestore
    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseFirestore.getInstance()

        // Comprobar si el usuario ya pertenece a una peña
        val idUsuarioActual = auth.currentUser?.uid
        if (idUsuarioActual != null) {
            database.collection("Usuarios").document(idUsuarioActual).get()
                .addOnSuccessListener { document ->
                    val idPena = document.getString("idPeña")
                    if (!idPena.isNullOrEmpty()) {

                        // Si ya tiene una peña, desactiva el botón y cambia su estilo
                        binding.btnCrearPena.isEnabled = false
                        binding.btnCrearPena.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.verdeBoton
                            )
                        )
                        binding.btnCrearPena.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.white)
                        )
                    }
                }
        }

        // Botón para seleccionar imagen desde la galería
        binding.btnSubirFoto.setOnClickListener {
            seleccionarImagen()
        }

        // Botón para crear la peña
        binding.btnCrearPena.setOnClickListener {
            val nombre = binding.etNombrePena.text.toString().trim()
            val ubicacion = binding.etUbicacion.text.toString().trim()

            var esValido = true
            binding.etNombrePena.error = null
            binding.etFoto.error = null

            // Validar nombre vacío
            if (nombre.isEmpty()) {
                binding.etNombrePena.error = "Este campo es obligatorio"
                esValido = false
            }

            // Validar imagen no seleccionada
            if (imageUri == null) {
                binding.etFoto.error = "Debes seleccionar una imagen"
                esValido = false
            }

            if (esValido) {
                binding.etNombrePena.setText("")
                binding.etUbicacion.setText("")
                binding.etFoto.setText("")

                // Mostrar el diálogo de confirmación si todo está correcto
                mostrarDialogo(nombre, ubicacion)
            }
        }

        return binding.root
    }

    // Muestra un AlertDialog personalizado para confirmar la creación de la peña
    private fun mostrarDialogo(nombre: String, ubicacion: String) {
        val dialogo = layoutInflater.inflate(R.layout.dialogo_principal, null)

        val titulo = dialogo.findViewById<TextView>(R.id.tituloDialogo)
        val mensaje = dialogo.findViewById<TextView>(R.id.mensajeDialogo)
        val btnAceptar = dialogo.findViewById<TextView>(R.id.btnAceptar)
        val btnCancelar = dialogo.findViewById<TextView>(R.id.btnCancelar)

        titulo.text = "Crear peña"
        mensaje.text = "¿Deseas crear una nueva peña? Te convertirás en su representante."

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogo)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        btnCancelar.setOnClickListener {
            alertDialog.dismiss()
        }

        btnAceptar.setOnClickListener {
            if (ubicacion.isNotEmpty()) {
                // Si hay ubicación, obtener coordenadas antes de guardar
                obtenerCoordenadasYSubirPena(nombre, ubicacion)
            } else {
                // Si no hay ubicación, guardar directamente
                subirImagenYGuardarPena(nombre, "", null, null)
            }

            alertDialog.dismiss()
        }
    }

    // Abre la galería para seleccionar imagen
    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE)
    }

    // Resultado de la selección de imagen desde galería
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            if (imageUri != null) {
                // Obtener el nombre del archivo desde la URI y mostrarlo en el EditText
                val nombreArchivo = obtenerNombreArchivo(imageUri!!)
                binding.etFoto.setText(nombreArchivo)
            }
        }
    }

    // Obtener el nombre del archivo seleccionado desde la URI
    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "desconocido.jpg"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val i = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (i != -1) nombre = it.getString(i)
            }
        }
        return nombre
    }

    // Usar Geocoder para obtener latitud y longitud a partir de una dirección
    private fun obtenerCoordenadasYSubirPena(nombre: String, ubicacion: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val direccionCompleta = "$ubicacion, Pedrajas de San Esteban, España"
            val addresses = geocoder.getFromLocationName(direccionCompleta, 1)
            if (!addresses.isNullOrEmpty()) {
                val lat = addresses[0].latitude
                val lon = addresses[0].longitude
                subirImagenYGuardarPena(nombre, ubicacion, lat, lon)
            } else {
                subirImagenYGuardarPena(nombre, "", null, null)
            }
        } catch (e: Exception) {
            subirImagenYGuardarPena(nombre, "", null, null)
        }
    }

    // Subir la imagen al almacenamiento de Firebase
    private fun subirImagenYGuardarPena(
        nombre: String,
        ubicacion: String,
        lat: Double?,
        lon: Double?
    ) {
        val refImagen = storage.reference.child("imagenes/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            refImagen.putFile(uri).addOnSuccessListener {
                refImagen.downloadUrl.addOnSuccessListener { imageUrl ->
                    guardarPenaEnFirestore(nombre, ubicacion, lat, lon, imageUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Guardar los datos de la peña en la base de datos
    private fun guardarPenaEnFirestore(
        nombre: String,
        ubicacion: String,
        lat: Double?,
        lon: Double?,
        imageUrl: String
    ) {
        val idPena = database.collection("Penas").document().id
        val idRepresentante = auth.currentUser?.uid ?: return

        val pena = hashMapOf(
            "idPeña" to idPena,
            "nombre" to nombre,
            "ubicación" to ubicacion,
            "imagen" to imageUrl,
            "idRepresentante" to idRepresentante
        )

        // Añadir latitud y longitud si existen
        if (lat != null && lon != null) {
            pena["lat"] = lat.toString()
            pena["lon"] = lon.toString()
        }

        // Subir el documento a Firestore
        database.collection("Penas").document(idPena).set(pena).addOnSuccessListener {
            actualizarRolUsuario(idRepresentante, idPena)
        }
    }

    // Actualizar el usuario para marcarlo como representante de esta nueva peña
    private fun actualizarRolUsuario(userId: String, idPena: String) {
        val userRef = database.collection("Usuarios").document(userId)
        userRef.update("idPeña", idPena)
        userRef.update("rol", "representante").addOnSuccessListener {
            Toast.makeText(requireContext(), "Peña creada con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Constante para identificar la selección de imagen
    companion object {
        private const val REQUEST_CODE_IMAGE = 1
    }
}