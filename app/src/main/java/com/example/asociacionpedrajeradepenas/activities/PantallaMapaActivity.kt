package com.example.asociacionpedrajeradepenas.activities

import android.os.Bundle
import com.example.asociacionpedrajeradepenas.fragments.MapaFragment
import com.example.asociacionpedrajeradepenas.R
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