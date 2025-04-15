package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import com.example.asociacionpedrajeradepenas.databinding.ActivityCrearEventoBinding

class CrearEventoActivity : BaseActivity() {

    private lateinit var binding: ActivityCrearEventoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar reutilizando la lógica del BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar)

    }
}