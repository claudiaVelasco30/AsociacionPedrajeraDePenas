package com.example.asociacionpedrajeradepenas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class EventosRepAdapter(eventos: List<Map<String, Any>>) :
    RecyclerView.Adapter<EventosRepAdapter.EventoViewHolder>() {

    // Ordena los eventos por fecha descendente
    private val eventosOrdenados = eventos.sortedBy { evento ->
        val fecha = evento["fecha_hora"]
        when (fecha) {
            is com.google.firebase.Timestamp -> fecha.toDate().time
            is Date -> fecha.time
            else -> Long.MIN_VALUE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_eventos_rep, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosOrdenados[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventosOrdenados.size

    // ViewHolder que representa la vista de cada evento
    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombreEvento)
        private val imagen: ImageView = itemView.findViewById(R.id.imgEvento)
        private val btnUnirse: Button = itemView.findViewById(R.id.btnUnirseEvento)

        // Enlaza los datos del evento con los elementos de la vista
        fun bind(evento: Map<String, Any>) {
            val nombreEvento = evento["nombre"] as? String ?: "Evento sin nombre"
            val idEvento = evento["idEvento"] as? String ?: return

            nombre.text = nombreEvento

            val imagenUrl = evento["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            val db = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                db.collection("Usuarios").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val idPena = userDoc.getString("idPeña")

                        db.collection("Inscripciones")
                            .whereEqualTo("idEvento", idEvento)
                            .whereEqualTo("idPeña", idPena)
                            .get()
                            .addOnSuccessListener { docs ->
                                if (docs.isEmpty) {
                                    btnUnirse.isEnabled = true
                                    btnUnirse.text = "Unirse"

                                    btnUnirse.setOnClickListener {
                                        btnUnirse.isEnabled = false
                                        btnUnirse.setBackgroundColor(
                                            ContextCompat.getColor(
                                                itemView.context,
                                                R.color.verdeBoton
                                            )
                                        )

                                        val nuevaInscripcion = hashMapOf(
                                            "estado" to "pendiente",
                                            "idEvento" to idEvento,
                                            "idPeña" to idPena
                                        )

                                        db.collection("Inscripciones").add(nuevaInscripcion)
                                            .addOnSuccessListener { docRef ->
                                                db.collection("Inscripciones").document(docRef.id)
                                                    .update("idInscripcion", docRef.id)
                                                Toast.makeText(
                                                    itemView.context,
                                                    "Inscripción enviada",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    itemView.context,
                                                    "Error al inscribirse",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                btnUnirse.isEnabled = true
                                                btnUnirse.setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        itemView.context,
                                                        R.color.verdePrincipal
                                                    )
                                                )
                                            }
                                    }
                                } else {
                                    btnUnirse.isEnabled = false
                                    btnUnirse.setBackgroundColor(
                                        ContextCompat.getColor(
                                            itemView.context,
                                            R.color.verdeBoton
                                        )
                                    )
                                }
                            }
                    }
            }

        }

    }
}