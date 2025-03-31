package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding
    private var userRole: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Margen dinámico
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar)) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight - 8 , 0, 8)
            WindowInsetsCompat.CONSUMED
        }

        val nombreToolbar = binding.nombreToolbar

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        obtenerNombreUsuario()
        obtenerRolUsuario()
    }

    private fun obtenerRolUsuario() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Usuarios").document(user.uid) // Asegúrate de que "Usuarios" coincide con Firestore
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userRole = document.getString("rol") ?: "usuario" // Verifica que el campo "rol" está en Firestore
                        invalidateOptionsMenu() // Refrescar el menú después de obtener el rol
                    }
                }
        }
    }

    private fun obtenerNombreUsuario() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreUsuario = document.getString("nombre") ?: "Nombre"
                        binding.nombreToolbar.text = nombreUsuario // Actualizar la UI aquí
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        // Esconder opciones según el rol obtenido
        val isAdminOrRep = userRole == "administrador" || userRole == "representante"
        menu?.findItem(R.id.action_opciones)?.isVisible = isAdminOrRep

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mapa -> {
                // Abrir actividad de Mapa
                startActivity(Intent(this, PantallaMapaActivity::class.java))
                true
            }
            R.id.action_logout -> {
                // Cerrar sesión en Firebase
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            R.id.action_opciones -> {
                if (userRole == "administrador"){
                    startActivity(Intent(this, PantallaAdministradorActivity::class.java))
                } else if (userRole == "representante"){

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}