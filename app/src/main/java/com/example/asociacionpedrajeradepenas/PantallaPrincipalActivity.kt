package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaPrincipalBinding
import com.google.firebase.auth.FirebaseAuth

class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Eliminar título de la Toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navView2: BottomNavigationView = findViewById(R.id.nav_view)
        val navController2 = findNavController(R.id.nav_host_fragment_activity_pantalla_principal)

        // Configurar navegación
        navView2.setupWithNavController(navController2)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_pantalla_principal)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_eventos, R.id.navigation_penas, R.id.navigation_crear
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mapa -> {
                // Abrir actividad de Mapa
                //startActivity(Intent(this, MapaActivity::class.java))
                true
            }
            R.id.action_logout -> {
                // Cerrar sesión en Firebase
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}