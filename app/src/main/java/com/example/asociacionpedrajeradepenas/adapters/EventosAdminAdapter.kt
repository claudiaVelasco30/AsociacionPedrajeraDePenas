package com.example.asociacionpedrajeradepenas.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class EventosAdminAdapter(
    eventos: List<Map<String, Any>>,
    private val onEventoEliminado: () -> Unit // Función que se llama tras eliminar un evento
) : RecyclerView.Adapter<EventosAdminAdapter.EventoViewHolder>() {

    // Lista de eventos ordenada por fecha
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
            LayoutInflater.from(parent.context).inflate(R.layout.item_eventos_admin, parent, false)
        return EventoViewHolder(view, onEventoEliminado)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosOrdenados[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventosOrdenados.size

    // ViewHolder para cada evento
    class EventoViewHolder(itemView: View, private val onEventoEliminado: () -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombreEvento)
        private val imagen: ImageView = itemView.findViewById(R.id.imgEvento)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarEvento)

        // Asigna los datos del evento a la vista
        fun bind(evento: Map<String, Any>) {
            val context = itemView.context
            val idEvento = evento["idEvento"] as? String ?: return
            val nombreEvento = evento["nombre"] as? String ?: "Evento sin nombre"
            nombre.text = nombreEvento

            val imagenUrl = evento["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(context).load(imagenUrl).into(imagen)
            }

            btnEliminar.setOnClickListener {
                mostrarDialogoEliminarEvento(context, nombreEvento, idEvento)
            }
        }

        // Función para mostrar el diálogo de confirmación
        private fun mostrarDialogoEliminarEvento(
            context: Context,
            nombreEvento: String,
            idEvento: String
        ) {
            val dialogo = LayoutInflater.from(context).inflate(R.layout.dialogo_principal, null)

            val titulo = dialogo.findViewById<TextView>(R.id.tituloDialogo)
            val mensaje = dialogo.findViewById<TextView>(R.id.mensajeDialogo)
            val btnAceptar = dialogo.findViewById<TextView>(R.id.btnAceptar)
            val btnCancelar = dialogo.findViewById<TextView>(R.id.btnCancelar)

            titulo.text = "Eliminar evento"
            mensaje.text =
                "¿Deseas eliminar el evento \"$nombreEvento\"?\nTambién se eliminarán sus inscripciones."

            val alertDialog = AlertDialog.Builder(context)
                .setView(dialogo)
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            btnCancelar.setOnClickListener {
                alertDialog.dismiss()
            }

            btnAceptar.setOnClickListener {
                eliminarEvento(idEvento, context)
                alertDialog.dismiss()
            }
        }

        // Función para eliminar el evento y sus inscripciones
        private fun eliminarEvento(idEvento: String, context: Context) {
            val db = FirebaseFirestore.getInstance()

            // Eliminar todas las inscripciones relacionadas con el evento
            db.collection("Inscripciones")
                .whereEqualTo("idEvento", idEvento)
                .get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()
                    for (doc in snapshot.documents) {
                        batch.delete(doc.reference)
                    }

                    // Luego de borrar inscripciones, eliminar el evento
                    db.collection("Eventos").document(idEvento)
                        .delete()
                        .addOnSuccessListener {
                            batch.commit() // Ejecutar el borrado en lote
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT)
                                        .show()
                                    onEventoEliminado()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error al eliminar el evento",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }
    }
}
