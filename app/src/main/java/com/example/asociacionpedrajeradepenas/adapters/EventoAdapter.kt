package com.example.asociacionpedrajeradepenas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.R
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventoAdapter(eventos: List<Map<String, Any>>) :
    RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    // Ordena los eventos por fecha descendente
    private val eventosOrdenados = eventos.sortedBy { evento ->
        val fecha = evento["fecha_hora"]
        when (fecha) {
            is com.google.firebase.Timestamp -> fecha.toDate().time // Convertir Timestamp a milisegundos
            is Date -> fecha.time // Si ya es Date, tomar los milisegundos
            else -> Long.MIN_VALUE // Si no tiene fecha válida, se coloca al final
        }
    }

    // Crea la vista para cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    // Asocia los datos del evento con la vista del ViewHolder
    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosOrdenados[position]
        holder.bind(evento)
    }

    // Devuelve el número total de eventos
    override fun getItemCount(): Int = eventosOrdenados.size

    // Clase que representa el ViewHolder de cada evento
    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvTitulo)
        private val descripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val fechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        private val imagen: ImageView = itemView.findViewById(R.id.imgEvento)
        private val ubicacion: TextView = itemView.findViewById(R.id.tvUbicacion)
        private val listaPenas: TextView = itemView.findViewById(R.id.tvPenas)

        private val db = FirebaseFirestore.getInstance()

        // Asigna los datos del evento a las vistas
        fun bind(evento: Map<String, Any>) {
            nombre.text = evento["nombre"] as? String ?: "Evento sin nombre"
            descripcion.text = evento["descripcion"] as? String ?: "Sin descripción"
            ubicacion.text = evento["ubicacion"] as? String ?: "Ubicación no disponible"
            fechaHora.text = formatearFecha(evento["fecha_hora"])

            val imagenUrl = evento["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            val idEvento = evento["idEvento"] as? String ?: return
            mostrarPenasParticipantes(idEvento)
        }

        // Muestra los nombres de las peñas que participan
        private fun mostrarPenasParticipantes(idEvento: String) {
            db.collection("Inscripciones")
                .whereEqualTo("idEvento", idEvento)
                .whereEqualTo("estado", "aceptada")
                .get()
                .addOnSuccessListener { inscripciones ->
                    val idPenas = inscripciones.documents.mapNotNull { it.getString("idPeña") }

                    if (idPenas.isEmpty()) {
                        listaPenas.text = "Peñas que participan: Ninguna"
                        return@addOnSuccessListener
                    }

                    db.collection("Penas")
                        .whereIn(FieldPath.documentId(), idPenas)
                        .get()
                        .addOnSuccessListener { penas ->
                            val nombres = penas.documents.mapNotNull { it.getString("nombre") }
                            listaPenas.text = "Peñas que participan: ${nombres.joinToString(", ")}"
                        }
                }
        }

        // Formatea la fecha para mostrarla
        private fun formatearFecha(fechaOriginal: Any?): String {
            if (fechaOriginal == null) return "Sin fecha"

            return try {
                val date = when (fechaOriginal) {
                    is com.google.firebase.Timestamp -> fechaOriginal.toDate() // Convertir Timestamp a Date
                    is Date -> fechaOriginal
                    else -> return "Sin fecha"
                }

                val fechaConFormato =
                    SimpleDateFormat("d 'de' MMMM 'a las' H:mm 'h'", Locale("es", "ES"))
                fechaConFormato.format(date)
            } catch (e: Exception) {
                "Sin fecha"
            }
        }
    }
}