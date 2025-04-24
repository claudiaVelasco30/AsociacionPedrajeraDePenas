package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PenaAdapter(
    private val penas: List<Map<String, Any>>, // Lista de peñas
    private val idPenaUsuario: String?,
    private val onInfoClick: (Map<String, Any>) -> Unit, // Callback para el botón "Más información"
    private val onUnirseClick: (Map<String, Any>) -> Unit // Callback para el botón "Unirse"
) : RecyclerView.Adapter<PenaAdapter.PenaViewHolder>() {

    // Crea y devuelve una nueva instancia del ViewHolder para cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pena, parent, false)
        return PenaViewHolder(view)
    }

    // Enlaza los datos de una peña a la vista correspondiente en el ViewHolder
    override fun onBindViewHolder(holder: PenaViewHolder, position: Int) {
        val pena = penas[position]
        holder.bind(pena, idPenaUsuario, onInfoClick, onUnirseClick)
    }

    // Devuelve el número total de elementos en la lista
    override fun getItemCount(): Int = penas.size

    // ViewHolder que contiene y gestiona las vistas de cada ítem del RecyclerView
    class PenaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombrePena)
        private val imagen: ImageView = itemView.findViewById(R.id.imgPena)
        private val btnInfo: Button = itemView.findViewById(R.id.btnMasInfo)
        private val btnUnirse: Button = itemView.findViewById(R.id.btnUnirse)

        // Asocia los datos de la peña a las vistas y define el comportamiento de los botones
        fun bind(
            pena: Map<String, Any>,
            idPenaUsuario: String?,
            onInfoClick: (Map<String, Any>) -> Unit,
            onUnirseClick: (Map<String, Any>) -> Unit
        ) {
            nombre.text = pena["nombre"] as? String ?: "Sin nombre"
            val imagenUrl = pena["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            if (idPenaUsuario != null) {
                // Si ya tiene una peña, desactiva el botón y cambia su estilo
                btnUnirse.isEnabled = false
                btnUnirse.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.verdeBoton
                    )
                )
                btnUnirse.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                // Comprobar si hay una solicitud pendiente del usuario para esta peña
                val db = FirebaseFirestore.getInstance()
                val idUsuario = FirebaseAuth.getInstance().currentUser?.uid

                db.collection("Solicitudes")
                    .whereEqualTo("idUsuario", idUsuario)
                    .whereEqualTo("estado", "pendiente")
                    .get()
                    .addOnSuccessListener { documents: QuerySnapshot ->
                        if (!documents.isEmpty) {
                            // Si ya hay una solicitud pendiente, desactiva el botón
                            btnUnirse.isEnabled = false
                            btnUnirse.setBackgroundColor(
                                ContextCompat.getColor(
                                    itemView.context,
                                    R.color.verdeBoton
                                )
                            )
                            btnUnirse.setTextColor(
                                ContextCompat.getColor(
                                    itemView.context,
                                    R.color.white
                                )
                            )
                        }
                    }

            }

            btnInfo.setOnClickListener {
                onInfoClick(pena)
            }

            btnUnirse.setOnClickListener {
                onUnirseClick(pena)
            }
        }
    }
}





