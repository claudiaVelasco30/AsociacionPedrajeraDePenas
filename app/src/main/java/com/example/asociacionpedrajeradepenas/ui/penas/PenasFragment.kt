package com.example.asociacionpedrajeradepenas.ui.penas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.DetallePenaActivity
import com.example.asociacionpedrajeradepenas.PenaAdapter
import com.example.asociacionpedrajeradepenas.databinding.FragmentPenasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PenasFragment : Fragment() {

    private var _binding: FragmentPenasBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var penaAdapter: PenaAdapter
    private val auth = FirebaseAuth.getInstance()
    private val penasList = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPenasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvpenas.layoutManager = LinearLayoutManager(requireContext())
        cargarPenas()
    }

    private fun cargarPenas() {
        db.collection("Penas").get()
            .addOnSuccessListener { result ->
                val listaPenas = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val penaData = document.data.toMutableMap()
                    penaData["id"] = document.id
                    listaPenas.add(penaData)
                }
                penasList.clear()
                penasList.addAll(listaPenas)
                actualizarRecyclerView()
            }
    }

    private fun actualizarRecyclerView() {
        penaAdapter = PenaAdapter(penasList,
            onInfoClick = { pena -> abrirDetallePena(pena) },
            onUnirseClick = { pena -> mostrarDialogoUnirse(pena) }
        )
        binding.rvpenas.adapter = penaAdapter
    }

    private fun abrirDetallePena(pena: Map<String, Any>) {
        val intent =  Intent(requireContext(), DetallePenaActivity::class.java).apply {
            putExtra("idPeña", pena["id"] as? String)
            putExtra("nombre", pena["nombre"] as? String)
            putExtra("ubicacion", pena["ubicación"] as? String)
            putExtra("idRepresentante", pena["idRepresentante"] as? String)
            putExtra("imagen", pena["imagen"] as? String)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoUnirse(pena: Map<String, Any>) {
        val idUsuario = auth.currentUser?.uid ?: return

        // Verificar si el usuario ya pertenece a una peña
        db.collection("Usuarios").document(idUsuario).get().addOnSuccessListener { document ->
            val idPeñaUsuario = document.getString("idPeña")
            if (!idPeñaUsuario.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Ya perteneces a una peña", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Mostrar AlertDialog para confirmar unión
            AlertDialog.Builder(requireContext())
                .setTitle("Unirse a peña")
                .setMessage("¿Deseas realizar una solicitud para unirte a la peña ${pena["nombre"]}?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar") { _, _ ->
                    crearSolicitud(pena["id"] as String, idUsuario)
                }
                .show()
        }
    }

    private fun crearSolicitud(idPeña: String, idUsuario: String) {
        val solicitud = hashMapOf(
            "idPeña" to idPeña,
            "idUsuario" to idUsuario,
            "estado" to "pendiente"
        )

        db.collection("Solicitudes").add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



