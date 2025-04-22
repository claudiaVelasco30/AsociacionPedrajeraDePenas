package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.databinding.FragmentPenasAdminBinding
import com.google.firebase.firestore.FirebaseFirestore

class PenasAdminFragment : Fragment() {

    private var _binding: FragmentPenasAdminBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var penaAdapter: PenasAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPenasAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvpenasadmin.layoutManager = LinearLayoutManager(requireContext())

        // Llamamos al metodo que carga las peñas desde Firestore
        cargarPenas()
    }

    // Metodo para obtener la lista de peñas almacenadas en la colección "Penas" de Firestore
    private fun cargarPenas() {
        db.collection("Penas").get()
            .addOnSuccessListener { result ->
                val listaPenas = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val penaMap = document.data.toMutableMap()
                    penaMap["idPeña"] = document.id
                    listaPenas.add(penaMap)
                }

                // Inicializamos el adaptador pasándole la lista de peñas
                penaAdapter = PenasAdminAdapter(listaPenas) {
                    cargarPenas() // Recarga la lista si se realiza una acción
                }

                // Asignamos el adaptador al RecyclerView
                binding.rvpenasadmin.adapter = penaAdapter
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}