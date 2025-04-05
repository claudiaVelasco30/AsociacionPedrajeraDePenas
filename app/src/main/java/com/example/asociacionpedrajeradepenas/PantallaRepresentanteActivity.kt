package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaPrincipalBinding
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaRepresentanteBinding

class PantallaRepresentanteActivity : BaseActivity() {

    private lateinit var binding: ActivityPantallaRepresentanteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaRepresentanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar reutilizando la lógica del BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar)

    }
}