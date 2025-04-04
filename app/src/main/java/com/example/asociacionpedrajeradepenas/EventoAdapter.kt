package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventoAdapter(eventos: List<Map<String, Any>>) :
    RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    private val eventosOrdenados = eventos.sortedByDescending { evento ->
        val fecha = evento["fecha_hora"]
        when (fecha) {
            is com.google.firebase.Timestamp -> fecha.toDate().time // Convertir Timestamp a milisegundos
            is Date -> fecha.time
            else -> Long.MIN_VALUE // Si no hay fecha, ponerlo al final
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosOrdenados[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventosOrdenados.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val descripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val fechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        private val imagen: ImageView = itemView.findViewById(R.id.imgEvento)
        private val ubicacion: TextView = itemView.findViewById(R.id.tvUbicacion)

        fun bind(evento: Map<String, Any>) {
            titulo.text = evento["nombre"] as? String ?: "Evento sin nombre"
            descripcion.text = evento["descripcion"] as? String ?: "Sin descripción"
            ubicacion.text = evento["ubicacion"] as? String ?: "Ubicación no disponible"

            val fechaOriginal = evento["fecha_hora"]
            fechaHora.text = formatearFecha(fechaOriginal)

            val imagenUrl = evento["imagenUrl"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }
        }

        private fun formatearFecha(fechaOriginal: Any?): String {
            if (fechaOriginal == null) return "Sin fecha"

            return try {
                val date = when (fechaOriginal) {
                    is com.google.firebase.Timestamp -> fechaOriginal.toDate() // Convertir Timestamp a Date
                    is Date -> fechaOriginal
                    else -> return "Sin fecha"
                }

                val outputFormat =
                    SimpleDateFormat("d 'de' MMMM 'a las' h:mm a", Locale("es", "ES"))
                outputFormat.format(date)
            } catch (e: Exception) {
                "Sin fecha"
            }
        }
    }
}