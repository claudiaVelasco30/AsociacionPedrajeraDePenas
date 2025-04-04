package com.example.asociacionpedrajeradepenas.ui.crear

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
import android.widget.Toast
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
    private lateinit var userRole: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseFirestore.getInstance()

        binding.btnSubirFoto.setOnClickListener {
            seleccionarImagen()
        }

        binding.btnCrearPena.setOnClickListener {
            val nombre = binding.editTextNombrePena.text.toString().trim()
            val ubicacion = binding.editTextUbicacion.text.toString().trim()

            binding.editTextNombrePena.setText("")
            binding.editTextUbicacion.setText("")
            binding.editTextFoto.setText("")

            if (nombre.isNotEmpty() && imageUri != null) {
                if (ubicacion.isNotEmpty()) {
                    obtenerCoordenadasYSubirPena(nombre, ubicacion)
                } else {
                    subirImagenYGuardarPena(nombre, "", null, null)
                }
            } else {
                Toast.makeText(requireContext(), "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            if (imageUri != null) {
                // Obtener el nombre del archivo desde la URI y mostrarlo en el EditText
                val nombreArchivo = obtenerNombreArchivo(imageUri!!)
                binding.editTextFoto.setText(nombreArchivo) // Asigna el nombre al EditText
            }
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "desconocido.jpg"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) nombre = it.getString(index)
            }
        }
        return nombre
    }

    private fun obtenerCoordenadasYSubirPena(nombre: String, ubicacion: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val direccionCompleta = "$ubicacion, Pedrajas de San Esteban, España"
            val addresses = geocoder.getFromLocationName(direccionCompleta, 1)
            if (addresses != null && addresses.isNotEmpty()) {
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

    private fun subirImagenYGuardarPena(nombre: String, ubicacion: String, lat: Double?, lon: Double?) {
        val storageRef = storage.reference.child("imagenes/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    guardarPenaEnFirestore(nombre, ubicacion, lat, lon, imageUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarPenaEnFirestore(nombre: String, ubicacion: String, lat: Double?, lon: Double?, imageUrl: String) {
        val idPena = database.collection("Penas").document().id
        val idRepresentante = auth.currentUser?.uid ?: return

        val pena = hashMapOf(
            "idPeña" to idPena,
            "nombre" to nombre,
            "ubicación" to ubicacion,
            "imagen" to imageUrl,
            "idRepresentante" to idRepresentante
        )

        if (lat != null && lon != null) {
            pena["lat"] = lat.toString()
            pena["lon"] = lon.toString()
        }

        database.collection("Penas").document(idPena).set(pena).addOnSuccessListener {
            actualizarRolUsuario(idRepresentante, idPena)
            crearSolicitud(idPena, idRepresentante)
        }
    }

    private fun actualizarRolUsuario(userId: String, idPena: String) {
        val userRef = database.collection("Usuarios").document(userId)
        userRef.update("idPeña", idPena)
        userRef.update("rol", "representante").addOnSuccessListener {
            Toast.makeText(requireContext(), "Peña creada con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crearSolicitud(idPena: String, idUsuario: String) {
        val idSolicitud = database.collection("Solicitudes").document().id
        val solicitud = hashMapOf(
            "idSolicitud" to idSolicitud,
            "estado" to "pendiente",
            "idPeña" to idPena,
            "idUsuario" to idUsuario
        )

        database.collection("solicitudes").document(idSolicitud).set(solicitud)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_IMAGE = 1
    }
}