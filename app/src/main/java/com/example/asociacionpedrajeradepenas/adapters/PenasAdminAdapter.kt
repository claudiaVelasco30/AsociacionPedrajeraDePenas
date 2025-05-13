package com.example.asociacionpedrajeradepenas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.R
import com.google.firebase.firestore.FirebaseFirestore

class PenasAdminAdapter(
    private val penas: List<Map<String, Any>>,
    private val onPenaEliminada: () -> Unit
) : RecyclerView.Adapter<PenasAdminAdapter.PenaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_lista_penas, parent, false)
        return PenaViewHolder(view, onPenaEliminada)
    }

    override fun onBindViewHolder(holder: PenaViewHolder, position: Int) {
        val pena = penas[position]
        holder.bind(pena)
    }

    override fun getItemCount(): Int = penas.size

    // ViewHolder que representa cada ítem individual de la lista de peñas
    class PenaViewHolder(itemView: View, private val onPenaEliminada: () -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombrePena)
        private val imagen: ImageView = itemView.findViewById(R.id.imgPena)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarPena)

        // Metodo para rellenar los datos de la peña en las vistas correspondientes
        fun bind(pena: Map<String, Any>) {
            nombre.text = pena["nombre"] as? String ?: "Peña sin nombre"

            val imagenUrl = pena["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            // Configura el botón de eliminar
            btnEliminar.setOnClickListener {
                mostrarDialogoEliminar(pena)
            }
        }

        private fun mostrarDialogoEliminar(pena: Map<String, Any>) {
            val nombrePena = pena["nombre"].toString()
            val idPena = pena["idPeña"].toString()

            val dialogo = LayoutInflater.from(itemView.context)
                .inflate(R.layout.dialogo_principal, null)

            val titulo = dialogo.findViewById<TextView>(R.id.tituloDialogo)
            val mensaje = dialogo.findViewById<TextView>(R.id.mensajeDialogo)
            val btnAceptar = dialogo.findViewById<TextView>(R.id.btnAceptar)
            val btnCancelar = dialogo.findViewById<TextView>(R.id.btnCancelar)

            titulo.text = "Eliminar peña"
            mensaje.text = "¿Estás seguro de que quieres eliminar la peña \"$nombrePena\"?\n" +
                    "Se borrarán también los datos relacionados"

            val alertDialog = AlertDialog.Builder(itemView.context)
                .setView(dialogo)
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            btnCancelar.setOnClickListener {
                alertDialog.dismiss()
            }

            btnAceptar.setOnClickListener {
                eliminarPenaConDatos(idPena)
                alertDialog.dismiss()
            }
        }

        private fun eliminarPenaConDatos(idPena: String) {
            val db = FirebaseFirestore.getInstance()
            val context = itemView.context

            // Eliminar peña
            db.collection("Penas").document(idPena).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Peña eliminada", Toast.LENGTH_SHORT).show()

                    // Actualiza los usuarios que pertenecían a la peña
                    db.collection("Usuarios")
                        .whereEqualTo("idPeña", idPena)
                        .get()
                        .addOnSuccessListener { usuarios ->
                            for (usuario in usuarios) {
                                val userRef = db.collection("Usuarios").document(usuario.id)
                                val esRepresentante = usuario.getString("rol") == "representante"

                                val actualizaciones = mutableMapOf<String, Any?>("idPeña" to null)
                                if (esRepresentante) actualizaciones["rol"] = "usuario"

                                userRef.update(actualizaciones)
                            }
                        }

                    // Eliminar solicitudes relacionadas con la peña
                    db.collection("Solicitudes")
                        .whereEqualTo("idPeña", idPena)
                        .get()
                        .addOnSuccessListener { solicitudes ->
                            for (solicitud in solicitudes) {
                                db.collection("Solicitudes").document(solicitud.id).delete()
                            }
                        }

                    // Eliminar inscripciones relacionadas con la peña
                    db.collection("Inscripciones")
                        .whereEqualTo("idPeña", idPena)
                        .get()
                        .addOnSuccessListener { inscripciones ->
                            for (inscripcion in inscripciones) {
                                db.collection("Inscripciones").document(inscripcion.id).delete()
                            }
                        }

                    // Recargar lista
                    onPenaEliminada()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al eliminar la peña", Toast.LENGTH_SHORT).show()
                }
        }
    }
}