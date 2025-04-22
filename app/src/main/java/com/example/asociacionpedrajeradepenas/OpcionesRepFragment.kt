package com.example.asociacionpedrajeradepenas

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.asociacionpedrajeradepenas.databinding.FragmentOpcionesRepBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale
import java.util.UUID

class OpcionesRepFragment : Fragment() {

    private var _binding: FragmentOpcionesRepBinding? = null
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
        _binding = FragmentOpcionesRepBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseFirestore.getInstance()

        // Botón para cambiar al fragment de eventos del representante
        binding.btnUnirseEvento.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, EventosRepFragment())
                .addToBackStack(null)
                .commit()
        }

        // Botón para ver solicitudes de unión
        binding.btnSolicitudesUnion.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, SolicitudesRepFragment())
                .addToBackStack(null)
                .commit()
        }

        // Botón para seleccionar una imagen del dispositivo
        binding.btnSubirFoto.setOnClickListener {
            seleccionarImagen()
        }

        // Botón para modificar los datos de la peña
        binding.btnModificarInfo.setOnClickListener {
            val nombre = binding.etNombrePena.text.toString().trim()
            val ubicacion = binding.etUbicacion.text.toString().trim()

            binding.etNombrePena.setText("")
            binding.etUbicacion.setText("")
            binding.etFoto.setText("")

            // Si no se modifica nada muestra un aviso
            if (nombre.isEmpty() && ubicacion.isEmpty() && imageUri == null) {
                Toast.makeText(
                    requireContext(),
                    "No has modificado ningún campo",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Si se cambia la ubicación obtiene las coordenadas antes de actualizar
            if (ubicacion.isNotEmpty()) {
                obtenerCoordenadasYActualizarDatos(nombre, ubicacion)
            } else {
                // Si hay imagen, la sube y actualiza
                if (imageUri != null) {
                    subirImagenYActualizarDatos(nombre, "", null, null)
                } else {
                    // Solo actualiza nombre si es lo único modificado
                    actualizarDatosEnFirestore(nombre, "", null, null, "")
                }
            }
        }

        return binding.root
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
                val nombreArchivo = obtenerNombreArchivo(imageUri!!)
                binding.etFoto.setText(nombreArchivo)
            }
        }
    }

    // Obtiene el nombre del archivo a partir de la URI
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

    // Se utiliza Geocoder para convertir la ubicación en coordenadas
    private fun obtenerCoordenadasYActualizarDatos(nombre: String, ubicacion: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        val direccionCompleta = "$ubicacion, Pedrajas de San Esteban, España"
        val addresses = geocoder.getFromLocationName(direccionCompleta, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val lat = addresses[0].latitude
            val lon = addresses[0].longitude
            if (imageUri != null) {
                subirImagenYActualizarDatos(nombre, ubicacion, lat, lon)
            } else {
                actualizarDatosEnFirestore(nombre, ubicacion, lat, lon, "")
            }
        } else {
            actualizarDatosEnFirestore(nombre, "", null, null, "")
        }

    }

    // Sube la imagen a Firebase Storage y luego actualiza los datos
    private fun subirImagenYActualizarDatos(
        nombre: String,
        ubicacion: String,
        lat: Double?,
        lon: Double?
    ) {
        val storageRef = storage.reference.child("imagenes/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    actualizarDatosEnFirestore(nombre, ubicacion, lat, lon, imageUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Actualiza los datos en la colección "Penas" de Firestore
    private fun actualizarDatosEnFirestore(
        nombre: String,
        ubicacion: String,
        lat: Double?,
        lon: Double?,
        imageUrl: String
    ) {
        val userId = auth.currentUser?.uid ?: return

        database.collection("Usuarios").document(userId).get().addOnSuccessListener { document ->
            val idPena = document.getString("idPeña")
            if (!idPena.isNullOrEmpty()) {
                val penaRef = database.collection("Penas").document(idPena)

                val actualizaciones = mutableMapOf<String, Any>()

                if (nombre.isNotEmpty()) actualizaciones["nombre"] = nombre
                if (ubicacion.isNotEmpty()) actualizaciones["ubicación"] = ubicacion
                if (lat != null && lon != null) {
                    actualizaciones["lat"] = lat.toString()
                    actualizaciones["lon"] = lon.toString()
                }
                if (imageUrl.isNotEmpty()) actualizaciones["imagen"] = imageUrl

                penaRef.update(actualizaciones).addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Los datos se han actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al actualizar los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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