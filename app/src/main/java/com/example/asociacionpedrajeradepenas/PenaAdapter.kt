package com.example.asociacionpedrajeradepenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PenaAdapter(
    private val penas: List<Map<String, Any>>,
    private val onInfoClick: (Map<String, Any>) -> Unit,
    private val onUnirseClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<PenaAdapter.PenaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pena, parent, false)
        return PenaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PenaViewHolder, position: Int) {
        val pena = penas[position]
        holder.bind(pena, onInfoClick, onUnirseClick)
    }

    override fun getItemCount(): Int = penas.size

    class PenaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tvNombrePena)
        private val imagen: ImageView = itemView.findViewById(R.id.imgPena)
        private val btnInfo: Button = itemView.findViewById(R.id.btnMasInfo)
        private val btnUnirse: Button = itemView.findViewById(R.id.btnUnirse)

        fun bind(
            peña: Map<String, Any>,
            onInfoClick: (Map<String, Any>) -> Unit,
            onUnirseClick: (Map<String, Any>) -> Unit
        ) {
            nombre.text = peña["nombre"] as? String ?: "Sin nombre"
            val imagenUrl = peña["imagen"] as? String
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imagenUrl).into(imagen)
            }

            btnInfo.setOnClickListener {
                onInfoClick(peña)
            }

            btnUnirse.setOnClickListener {
                onUnirseClick(peña)
            }
        }
    }
}





