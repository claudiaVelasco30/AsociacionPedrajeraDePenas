package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaPrincipalBinding

class PantallaPrincipalActivity : BaseActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la Toolbar reutilizando la lógica del BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)

        // Configuración de la navegación inferior
        val navController = findNavController(R.id.nav_host_fragment_activity_pantalla_principal)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_eventos, R.id.navigation_penas, R.id.navigation_crear)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

    }
}