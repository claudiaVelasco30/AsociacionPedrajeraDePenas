package com.example.asociacionpedrajeradepenas.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.databinding.FragmentSolicitudesRepBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SolicitudesRepFragment : Fragment() {

    private var _binding: FragmentSolicitudesRepBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var solicitudAdapter: SolicitudesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolicitudesRepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvsolicitudesrep.layoutManager = LinearLayoutManager(requireContext())

        // Se llama a la función que carga las solicitudes desde Firestore
        cargarSolicitudes()
    }

    // Función que carga las solicitudes pendientes de unión para la peña del representante
    private fun cargarSolicitudes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Obtener el documento del usuario actual para saber a qué peña pertenece
        db.collection("Usuarios").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val idPenaUsuario = userDoc.getString("idPeña")

                // Filtrar solicitudes que están pendientes y son para la peña del usuario
                db.collection("Solicitudes")
                    .whereEqualTo("estado", "pendiente")
                    .whereEqualTo("idPena", idPenaUsuario)
                    .get()
                    .addOnSuccessListener { result ->
                        val listaSolicitudes = mutableListOf<Map<String, String>>()

                        if (result.isEmpty) {
                            Toast.makeText(
                                requireContext(),
                                "No hay solicitudes pendientes",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.rvsolicitudesrep.adapter = null
                        }

                        var procesadas =
                            0 // Variable para controlar cuando todas las solicitudes están procesadas

                        for (document in result.documents) {
                            val idUsuario = document.getString("idUsuario") ?: continue
                            val idSolicitud = document.id
                            val idPena = document.getString("idPena") ?: continue

                            // Se obtienen los datos del usuario que hizo la solicitud
                            db.collection("Usuarios").document(idUsuario).get()
                                .addOnSuccessListener { userSnapshot ->
                                    val nombreUsuario =
                                        userSnapshot.getString("nombre") ?: "Sin nombre"
                                    val apellidosUsuario = userSnapshot.getString("apellidos") ?: ""

                                    // Crear un mapa con los datos necesarios para el adaptador
                                    val solicitudMap = mapOf(
                                        "idSolicitud" to idSolicitud,
                                        "idUsuario" to idUsuario,
                                        "idPena" to idPena,
                                        "nombreUsuario" to nombreUsuario,
                                        "apellidosUsuario" to apellidosUsuario
                                    )

                                    listaSolicitudes.add(solicitudMap)
                                }
                                .addOnCompleteListener {
                                    procesadas++
                                    if (procesadas == result.size()) {
                                        solicitudAdapter = SolicitudesAdapter(listaSolicitudes) {
                                            cargarSolicitudes() // Se recargan las solicitudes al aceptar o rechazar
                                        }
                                        binding.rvsolicitudesrep.adapter = solicitudAdapter
                                    }
                                }
                        }
                    }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}