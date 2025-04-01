package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class EventoAdapter(private val eventos: List<Map<String, Any>>) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val descripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val fechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        private val imagen: ImageView = itemView.findViewById(R.id.imgEvento)

        fun bind(evento: Map<String, Any>) {
            titulo.text = evento["nombre"] as? String ?: "Evento sin título"
            descripcion.text = evento["descripcion"] as? String ?: "Sin descripción"

            val fechaOriginal = evento["fecha_hora"] as? String ?: ""
            fechaHora.text = formatearFecha(fechaOriginal)

            val imagenUrl = evento["imagenUrl"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }
        }

        private fun formatearFecha(fechaOriginal: String): String {
            return try {
                val inputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm:ss a z", Locale("es", "ES"))
                val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy 'a las' h:mm a", Locale("es", "ES"))
                val date = inputFormat.parse(fechaOriginal)
                date?.let { outputFormat.format(it) } ?: "Fecha no disponible"
            } catch (e: Exception) {
                "Fecha no disponible"
            }
        }
    }
}