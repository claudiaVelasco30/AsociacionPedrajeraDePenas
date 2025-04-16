package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class PenasAdminAdapter(private val penas:List<Map<String, Any>>,
                        private val onPenaEliminada: () -> Unit
) : RecyclerView.Adapter<PenasAdminAdapter.PenaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lista_penas, parent, false)
        return PenaViewHolder(view, onPenaEliminada)
    }

    override fun onBindViewHolder(holder: PenaViewHolder, position: Int) {
        val pena = penas[position]
        holder.bind(pena)
    }

    override fun getItemCount(): Int = penas.size

    class PenaViewHolder(itemView: View, private val onPenaEliminada: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombrePena)
        private val imagen: ImageView = itemView.findViewById(R.id.imgPena)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarPena)

        fun bind(pena: Map<String, Any>) {
            nombre.text = pena["nombre"] as? String ?: "Peña sin nombre"

            val imagenUrl = pena["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            btnEliminar.setOnClickListener {
                val context = itemView.context
                val id = pena["idPeña"] as? String
                if (id != null) {
                    FirebaseFirestore.getInstance().collection("Penas").document(id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Peña eliminada", Toast.LENGTH_SHORT).show()
                            onPenaEliminada()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al eliminar la peña", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}