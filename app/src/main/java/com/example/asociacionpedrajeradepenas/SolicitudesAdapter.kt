package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SolicitudesAdapter(
    private val solicitudes: List<Map<String, Any>>,
    private val onSolicitudEliminada: () -> Unit
) : RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitudes_rep, parent, false)
        return SolicitudViewHolder(view, onSolicitudEliminada)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = solicitudes[position]
        holder.bind(solicitud)
    }

    override fun getItemCount(): Int = solicitudes.size

    // ViewHolder interno que representa cada tarjeta de solicitud
    class SolicitudViewHolder(itemView: View, private val onSolicitudEliminada: () -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val nombre: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        private val btnAceptar = itemView.findViewById<Button>(R.id.btnAceptarSolicitud)
        private val btnRechazar = itemView.findViewById<Button>(R.id.btnRechazarSolicitud)
        private val db = FirebaseFirestore.getInstance()

        // Metodo para vincular los datos de una solicitud a la vista
        fun bind(solicitud: Map<String, Any>) {
            val idSolicitud = solicitud["idSolicitud"] as? String
            val idUsuario = solicitud["idUsuario"] as? String
            val idPena = solicitud["idPeña"] as? String
            val nombreUsuario = solicitud["nombreUsuario"]
            val apellidoUsuario = solicitud["apellidosUsuario"]

            nombre.text = "$nombreUsuario $apellidoUsuario"

            btnAceptar.setOnClickListener {
                if (idSolicitud != null && idUsuario != null && idPena != null) {
                    val solicitudRef = db.collection("Solicitudes").document(idSolicitud)
                    val usuarioRef = db.collection("Usuarios").document(idUsuario)

                    // Actualizar estado de la solicitud
                    solicitudRef.update("estado", "aceptada")
                        .addOnSuccessListener {
                            // Si la solicitud se actualiza correctamente se actualiza el idPeña del usuario
                            usuarioRef.update("idPeña", idPena)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        itemView.context,
                                        "Solicitud aceptada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSolicitudEliminada()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                itemView.context,
                                "Error al aceptar la solicitud",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            btnRechazar.setOnClickListener {
                db.collection("Solicitudes").document(idSolicitud.toString())
                    .update("estado", "rechazada")
                    .addOnSuccessListener {
                        Toast.makeText(itemView.context, "Solicitud rechazada", Toast.LENGTH_SHORT)
                            .show()
                        onSolicitudEliminada() // Recargar solicitudes
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            itemView.context,
                            "Error al rechazar la solicitud",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}