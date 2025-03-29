package com.example.asociacionpedrajeradepenas.ui.crear

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
        }
    }

    private fun obtenerCoordenadasYSubirPena(nombre: String, ubicacion: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(ubicacion, 1)
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
        val idPena = database.collection("penas").document().id
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

        database.collection("penas").document(idPena).set(pena).addOnSuccessListener {
            actualizarRolUsuario(idRepresentante)
            crearSolicitud(idPena, idRepresentante)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al guardar la peña", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarRolUsuario(userId: String) {
        val userRef = database.collection("usuarios").document(userId)
        userRef.update("rol", "representante").addOnSuccessListener {
            Toast.makeText(requireContext(), "Peña creada con éxito", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al actualizar el rol del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crearSolicitud(idPena: String, idUsuario: String) {
        val idSolicitud = database.collection("solicitudes").document().id
        val solicitud = hashMapOf(
            "idSolicitud" to idSolicitud,
            "estado" to "pendiente",
            "idPeña" to idPena,
            "idUsuario" to idUsuario
        )

        database.collection("solicitudes").document(idSolicitud).set(solicitud).addOnFailureListener {
            Toast.makeText(requireContext(), "Error al crear la solicitud", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}