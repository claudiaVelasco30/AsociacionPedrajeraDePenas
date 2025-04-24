package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.databinding.FragmentPenasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PenasFragment : Fragment() {

    private var _binding: FragmentPenasBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var penaAdapter: PenaAdapter
    private val penasList = mutableListOf<Map<String, Any>>()
    private var idPenaUsuario: String? = null

    // Se infla la vista del fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPenasBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configuración del RecyclerView al crear la vista
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvpenas.layoutManager = LinearLayoutManager(requireContext())

        // Obtener el id del usuario
        val idUsuario = FirebaseAuth.getInstance().currentUser?.uid

        if (idUsuario != null) {
            db.collection("Usuarios").document(idUsuario).get()
                .addOnSuccessListener { document ->
                    idPenaUsuario = document.getString("idPeña")

                    // Carga las peñas desde Firestore
                    cargarPenas()
                }
        }
    }

    // Carga los documentos de la colección 'Penas' desde Firestore
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

                // Se muestra la lista en el RecyclerView
                actualizarRecyclerView()
            }
    }

    // Configuración del RecyclerView con el adaptador y los listeners para los botones
    private fun actualizarRecyclerView() {
        penaAdapter = PenaAdapter(
            penasList,
            idPenaUsuario,
            onInfoClick = { pena -> abrirDetallePena(pena) },
            onUnirseClick = { pena -> mostrarDialogoUnirse(pena) }
        )
        binding.rvpenas.adapter = penaAdapter
    }

    // Abre una nueva Activity con los detalles de la peña seleccionada
    private fun abrirDetallePena(pena: Map<String, Any>) {
        val intent = Intent(requireContext(), DetallePenaActivity::class.java).apply {
            putExtra("idPeña", pena["idPeña"] as? String)
            putExtra("nombre", pena["nombre"] as? String)
            putExtra("ubicacion", pena["ubicación"] as? String)
            putExtra("idRepresentante", pena["idRepresentante"] as? String)
            putExtra("imagen", pena["imagen"] as? String)
        }
        startActivity(intent)
    }

    // Muestra un AlertDialog personalizado para confirmar la unión a la peña
    private fun mostrarDialogoUnirse(pena: Map<String, Any>) {
        val nombrePena = pena["nombre"].toString()
        val idPena = pena["idPeña"].toString()

        val dialogo = layoutInflater.inflate(R.layout.dialogo_principal, null)

        val titulo = dialogo.findViewById<TextView>(R.id.tituloDialogo)
        val mensaje = dialogo.findViewById<TextView>(R.id.mensajeDialogo)
        val btnAceptar = dialogo.findViewById<TextView>(R.id.btnAceptar)
        val btnCancelar = dialogo.findViewById<TextView>(R.id.btnCancelar)

        titulo.text = "Unirse a peña"
        mensaje.text = "¿Deseas realizar una solicitud para unirte a la peña \"$nombrePena\"?"

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogo)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        btnCancelar.setOnClickListener {
            alertDialog.dismiss()
        }

        btnAceptar.setOnClickListener {
            val idUsuario = FirebaseAuth.getInstance().currentUser?.uid
            if (idUsuario != null) {
                crearSolicitud(idPena, idUsuario)
            }
            alertDialog.dismiss()
        }
    }

    // Crea un documento en la colección 'Solicitudes' con los datos del usuario y la peña
    private fun crearSolicitud(idPena: String, idUsuario: String) {
        val database = FirebaseFirestore.getInstance()
        val idSolicitud = database.collection("Solicitudes").document().id

        val solicitud = hashMapOf(
            "idSolicitud" to idSolicitud,
            "idPena" to idPena,
            "idUsuario" to idUsuario,
            "estado" to "pendiente"
        )

        db.collection("Solicitudes").add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show()
                cargarPenas()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al enviar solicitud", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



