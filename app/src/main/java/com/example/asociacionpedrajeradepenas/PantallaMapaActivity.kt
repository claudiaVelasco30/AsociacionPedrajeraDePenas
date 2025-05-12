package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaMapaBinding

class PantallaMapaActivity : BaseActivity() {

    private lateinit var binding: ActivityPantallaMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se configura el toolbar personalizado con el metodo definido en BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)

        // Carga el fragmento del mapa dentro del contenedor
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorMapa, MapaFragment())
            .commit()
    }
}