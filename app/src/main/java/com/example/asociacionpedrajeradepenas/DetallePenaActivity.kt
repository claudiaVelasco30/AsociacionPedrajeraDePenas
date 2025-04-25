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

        // Recupera los datos enviados desde la actividad anterior mediante el intent
        val idPeña = intent.getStringExtra("idPeña") ?: return
        val nombre = intent.getStringExtra("nombre") ?: ""
        val ubicacion = intent.getStringExtra("ubicacion") ?: ""
        val representante =
            obtenerNombreRepresentante(intent.getStringExtra("idRepresentante") ?: "Desconocido")
        val imagenUrl = intent.getStringExtra("imagen")

        // Asigna los datos recibidos a los elementos de la interfaz
        binding.tvNombrePena.text = nombre
        binding.tvUbicacionPena.text = ubicacion
        binding.tvRepresentantePena.text = representante.toString()

        if (!imagenUrl.isNullOrEmpty()) {
            Glide.with(this).load(imagenUrl).into(binding.imgPena)
        }

        // Carga la lista de integrantes desde Firestore
        cargarIntegrantes(idPeña)

        // Al pulsar el botón, se abre la pantalla del mapa
        binding.btnMapa.setOnClickListener {
            val intent = Intent(this, PantallaMapaActivity::class.java)
            intent.putExtra("idPenaMostrar", idPeña)
            startActivity(intent)
        }
    }

    // Función que carga los integrantes de la peña desde la colección "Usuarios"
    private fun cargarIntegrantes(idPeña: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Usuarios").whereEqualTo("idPeña", idPeña).get()
            .addOnSuccessListener { result ->
                val listaIntegrantes = result.documents.mapNotNull {
                    it.getString("nombre") + " " + it.getString("apellidos")
                }
                binding.tvIntegrantesPena.text = listaIntegrantes.joinToString("\n")
            }
    }

    // Función que obtiene el nombre completo del representante a partir de su id
    private fun obtenerNombreRepresentante(idRepresentante: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Usuarios").document(idRepresentante).get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre") ?: "Desconocido"
                val apellidos = document.getString("apellidos") ?: ""
                binding.tvRepresentantePena.text = "Representante: $nombre $apellidos"
            }
            .addOnFailureListener {
                binding.tvRepresentantePena.text = "Desconocido"
            }
    }
}
