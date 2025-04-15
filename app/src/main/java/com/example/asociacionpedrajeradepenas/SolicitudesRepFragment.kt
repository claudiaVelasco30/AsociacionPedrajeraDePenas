package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    ): View? {
        _binding = FragmentSolicitudesRepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvsolicitudesrep.layoutManager = LinearLayoutManager(requireContext())
        cargarSolicitudes()
    }

    private fun cargarSolicitudes() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Usuarios").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val idPenaUsuario = userDoc.getString("idPeña")

                db.collection("Solicitudes").get()
                    .addOnSuccessListener { result ->
                        val listaSolicitudes = mutableListOf<Map<String, String>>()
                        val solicitudesPendientes = result.documents.filter {
                            it.getString("estado") == "pendiente" &&
                                    it.getString("idPeña") == idPenaUsuario
                        }

                        var procesadas = 0
                        for (document in solicitudesPendientes) {
                            val idUsuario = document.getString("idUsuario") ?: continue
                            val idSolicitud = document.id

                            db.collection("Usuarios").document(idUsuario).get()
                                .addOnSuccessListener { userSnapshot ->
                                    val nombreUsuario = userSnapshot.getString("nombre") ?: "Sin nombre"
                                    val apellidosUsuario = userSnapshot.getString("apellidos") ?: ""

                                    val solicitudMap = mapOf(
                                        "idSolicitud" to idSolicitud,
                                        "idUsuario" to idUsuario,
                                        "nombreUsuario" to nombreUsuario,
                                        "apellidosUsuario" to apellidosUsuario
                                    )

                                    listaSolicitudes.add(solicitudMap)
                                }
                                .addOnCompleteListener {
                                    procesadas++
                                    if (procesadas == solicitudesPendientes.size) {
                                        solicitudAdapter = SolicitudesAdapter(listaSolicitudes){
                                            cargarSolicitudes()
                                        }
                                        binding.rvsolicitudesrep.adapter = solicitudAdapter
                                    }
                                }
                        }
                    }
            }
    }

}