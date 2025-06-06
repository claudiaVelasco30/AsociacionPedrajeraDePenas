package com.example.asociacionpedrajeradepenas.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.adapters.EventosAdminAdapter
import com.example.asociacionpedrajeradepenas.activities.CrearEventoActivity
import com.example.asociacionpedrajeradepenas.databinding.FragmentEventosAdminBinding
import com.google.firebase.firestore.FirebaseFirestore

class EventosAdminFragment : Fragment() {

    private var _binding: FragmentEventosAdminBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var eventoAdapter: EventosAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventosAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rveventosadmin.layoutManager = LinearLayoutManager(requireContext())

        // Se cargan los eventos desde Firestore
        cargarEventos()

        // Al hacer clic en el botón "Crear Evento", se abre la actividad correspondiente
        binding.btnCrearEvento.setOnClickListener {
            startActivity(Intent(requireContext(), CrearEventoActivity::class.java))
        }
    }

    // Función para obtener los eventos de Firestore y mostrarlos en el RecyclerView
    private fun cargarEventos() {
        db.collection("Eventos").get()
            .addOnSuccessListener { result ->
                val listaEventos = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val eventoMap = document.data.toMutableMap()
                    eventoMap["idEvento"] = document.id
                    listaEventos.add(eventoMap)
                }

                // Se crea y asigna el adaptador al RecyclerView
                eventoAdapter = EventosAdminAdapter(listaEventos) {
                    cargarEventos() // Se recarga la lista tras eliminar un evento
                }
                binding.rveventosadmin.adapter = eventoAdapter
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}