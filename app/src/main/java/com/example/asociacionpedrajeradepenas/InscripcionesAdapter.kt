package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class InscripcionesAdapter (private val inscripciones: List<Map<String, Any>>,
                            private val onInscripcionEliminada: () -> Unit
) : RecyclerView.Adapter<InscripcionesAdapter.InscripcionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InscripcionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inscripciones_admin, parent, false)
        return InscripcionViewHolder(view, onInscripcionEliminada)
    }

    override fun onBindViewHolder(holder: InscripcionViewHolder, position: Int) {
        val inscripcion = inscripciones[position]
        holder.bind(inscripcion)
    }

    override fun getItemCount(): Int = inscripciones.size

    class InscripcionViewHolder(itemView: View, private val onInscripcionEliminada: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val texto: TextView = itemView.findViewById(R.id.tvMensaje)
        private val nombre: TextView = itemView.findViewById(R.id.tvNombrePena)
        private val imagen: ImageView = itemView.findViewById(R.id.imgPena)
        private val btnAceptar = itemView.findViewById<Button>(R.id.btnAceptar)
        private val btnRechazar = itemView.findViewById<Button>(R.id.btnRechazar)
        private val db = FirebaseFirestore.getInstance()

        fun bind(inscripcion: Map<String, Any>) {
            val nombrePena = inscripcion["nombrePena"] ?: "Peña desconocida"
            val nombreEvento = inscripcion["nombreEvento"] ?: "Evento desconocido"
            val idInscripcion = inscripcion["idInscripcion"]
            val imagenUrl = inscripcion["imagenPena"] as? String

            texto.text = "La peña $nombrePena quiere unirse al evento $nombreEvento"
            nombre.text = nombrePena.toString()

            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            btnAceptar.setOnClickListener {
                idInscripcion?.let { id ->
                    db.collection("Inscripciones").document(id.toString())
                        .update("estado", "aceptada")
                        .addOnSuccessListener {
                            Toast.makeText(itemView.context, "Inscripción aceptada", Toast.LENGTH_SHORT).show()
                            onInscripcionEliminada()
                        }
                        .addOnFailureListener {
                            Toast.makeText(itemView.context, "Error al aceptar la inscripción", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            btnRechazar.setOnClickListener {
                idInscripcion?.let { id ->
                    db.collection("Inscripciones").document(id.toString())
                        .update("estado", "rechazada")
                        .addOnSuccessListener {
                            Toast.makeText(itemView.context, "Inscripción rechazada", Toast.LENGTH_SHORT).show()
                            onInscripcionEliminada()
                        }
                        .addOnFailureListener {
                            Toast.makeText(itemView.context, "Error al rechazar la inscripción", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}