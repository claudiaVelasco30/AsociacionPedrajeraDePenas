package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.databinding.FragmentInscripcionesAdminBinding
import com.google.firebase.firestore.FirebaseFirestore

class InscripcionesAdminFragment : Fragment() {

    private var _binding: FragmentInscripcionesAdminBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var inscripcionAdapter: InscripcionesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInscripcionesAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //CAMBIAR
        binding.rvsolicitudesadmin.layoutManager = LinearLayoutManager(requireContext())
        cargarInscripciones()
    }

    private fun cargarInscripciones() {
        db.collection("Inscripciones").get()
            .addOnSuccessListener { result ->
                val listaInscripciones = mutableListOf<Map<String, String>>()
                val inscripcionesPendientes = result.documents.filter {
                    it.getString("estado") == "pendiente"
                }

                for (document in inscripcionesPendientes) {
                    val idPena = document.getString("idPeña") ?: continue
                    val idEvento = document.getString("idEvento") ?: continue

                    db.collection("Penas").document(idPena).get().addOnSuccessListener { penaDoc ->
                        val nombrePena = penaDoc.getString("nombre") ?: "Peña desconocida"

                        db.collection("Eventos").document(idEvento).get()
                            .addOnSuccessListener { eventoDoc ->
                                val nombreEvento =
                                    eventoDoc.getString("nombre") ?: "Evento desconocido"

                                val inscripcionMap = mapOf(
                                    "idInscripcion" to document.id,
                                    "nombrePena" to nombrePena,
                                    "nombreEvento" to nombreEvento,
                                    "imagenPena" to penaDoc.getString("imagen").orEmpty()
                                )
                                listaInscripciones.add(inscripcionMap)

                                inscripcionAdapter = InscripcionesAdapter(listaInscripciones){
                                    cargarInscripciones()
                                }
                                binding.rvsolicitudesadmin.adapter = inscripcionAdapter

                            }
                    }
                }
            }
    }
}