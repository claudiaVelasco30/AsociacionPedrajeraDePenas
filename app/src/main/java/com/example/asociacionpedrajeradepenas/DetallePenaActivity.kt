package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.databinding.ActivityDetallePenaBinding
import com.google.firebase.firestore.FirebaseFirestore

class DetallePenaActivity : BaseActivity() {

    private lateinit var binding: ActivityDetallePenaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)

        val idPeña = intent.getStringExtra("idPeña") ?: return
        val nombre = intent.getStringExtra("nombre") ?: ""
        val ubicacion = intent.getStringExtra("ubicacion") ?: ""
        val representante = obtenerNombreRepresentante(intent.getStringExtra("idRepresentante") ?: "Desconocido")
        val imagenUrl = intent.getStringExtra("imagen")

        binding.tvNombrePena.text = nombre
        binding.tvUbicacionPena.text = ubicacion
        binding.tvRepresentantePena.text = representante.toString()

        if (!imagenUrl.isNullOrEmpty()) {
            Glide.with(this).load(imagenUrl).into(binding.imgPena)
        }

        cargarIntegrantes(idPeña)

        binding.btnMapa.setOnClickListener {
            startActivity(Intent(this, PantallaMapaActivity::class.java))
        }
    }

    private fun cargarIntegrantes(idPeña: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Usuarios").whereEqualTo("idPeña", idPeña).get()
            .addOnSuccessListener { result ->
                val listaIntegrantes = result.documents.mapNotNull {" - " + it.getString("nombre") + " " + it.getString("apellidos") }
                binding.tvIntegrantesPena.text = listaIntegrantes.joinToString("\n")
            }
    }

    private fun obtenerNombreRepresentante(idRepresentante: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Usuarios").document(idRepresentante).get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre") ?: "Desconocido"
                val apellidos = document.getString("apellidos") ?: ""
                binding.tvRepresentantePena.text = "$nombre $apellidos"
            }
            .addOnFailureListener {
                binding.tvRepresentantePena.text = "Desconocido"
            }
    }
}
