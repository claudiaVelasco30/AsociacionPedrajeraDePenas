package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
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
        cargarEventos()
    }

    private fun cargarEventos() {
        db.collection("Eventos").get()
            .addOnSuccessListener { result ->
                val listaEventos = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    listaEventos.add(document.data)
                }
                eventoAdapter = EventosAdminAdapter(listaEventos)
                binding.rveventosadmin.adapter = eventoAdapter
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}