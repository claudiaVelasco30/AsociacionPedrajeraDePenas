package com.example.asociacionpedrajeradepenas.activities

import android.os.Bundle
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaRepresentanteBinding

class PantallaRepresentanteActivity : BaseActivity() {

    private lateinit var binding: ActivityPantallaRepresentanteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaRepresentanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la Toolbar reutilizando la lógica del BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)
    }
}