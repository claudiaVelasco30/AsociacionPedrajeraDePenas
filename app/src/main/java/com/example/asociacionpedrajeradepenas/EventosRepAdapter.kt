package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Date

class EventosRepAdapter (eventos: List<Map<String, Any>>) :
    RecyclerView.Adapter<EventosRepAdapter.EventoViewHolder>() {

    private val eventosOrdenados = eventos.sortedByDescending { evento ->
        val fecha = evento["fecha_hora"]
        when (fecha) {
            is com.google.firebase.Timestamp -> fecha.toDate().time
            is Date -> fecha.time
            else -> Long.MIN_VALUE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_eventos_rep, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosOrdenados[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventosOrdenados.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.nombreEvento)
        private val imagen: ImageView = itemView.findViewById(R.id.imagenEvento)

        fun bind(evento: Map<String, Any>) {
            nombre.text = evento["nombre"] as? String ?: "Evento sin nombre"

            val imagenUrl = evento["imagenUrl"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }
        }
    }
}