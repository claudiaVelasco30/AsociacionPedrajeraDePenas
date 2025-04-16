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
import java.util.Date

class EventosAdminAdapter(eventos: List<Map<String, Any>>,
                          private val onEventoEliminado: () -> Unit
) : RecyclerView.Adapter<EventosAdminAdapter.EventoViewHolder>() {

    private val eventosOrdenados = eventos.sortedByDescending { evento ->
        val fecha = evento["fecha_hora"]
        when (fecha) {
            is com.google.firebase.Timestamp -> fecha.toDate().time
            is Date -> fecha.time
            else -> Long.MIN_VALUE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_eventos_admin, parent, false)
        return EventoViewHolder(view, onEventoEliminado)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosOrdenados[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventosOrdenados.size

    class EventoViewHolder(itemView: View, private val onEventoEliminado: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombreEvento)
        private val imagen: ImageView = itemView.findViewById(R.id.imgEvento)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarEvento)

        fun bind(evento: Map<String, Any>) {
            nombre.text = evento["nombre"] as? String ?: "Evento sin nombre"

            val imagenUrl = evento["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            btnEliminar.setOnClickListener {
                val context = itemView.context
                val id = evento["idEvento"] as? String
                if (id != null) {
                    FirebaseFirestore.getInstance().collection("Eventos").document(id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                            onEventoEliminado()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al eliminar el evento", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}