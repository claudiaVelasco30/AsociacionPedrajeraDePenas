package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {

    protected lateinit var auth: FirebaseAuth
    protected lateinit var db: FirebaseFirestore
    protected var userRole: String = "usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        obtenerRolUsuario()
    }

    protected fun setupToolbar(toolbar: Toolbar, nombreToolbar: TextView) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Ajustar margen dinámico para evitar superposición con el status bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight - 8, 0, 8)
            WindowInsetsCompat.CONSUMED
        }

        // Obtener y mostrar el nombre del usuario
        obtenerNombreUsuario(nombreToolbar)
    }

    private fun obtenerNombreUsuario(nombreToolbar: TextView) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreUsuario = document.getString("nombre") ?: "Nombre"
                        nombreToolbar.text = nombreUsuario
                    }
                }
        }
    }

    private fun obtenerRolUsuario() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userRole = document.getString("rol") ?: "usuario"
                        invalidateOptionsMenu() // Refrescar menú tras obtener el rol
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
                startActivity(Intent(this, PantallaMapaActivity::class.java))
                true
            }
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            R.id.action_opciones -> {
                if (userRole == "administrador") {
                    startActivity(Intent(this, PantallaAdministradorActivity::class.java))
                } else if (userRole == "representante") {
                    // Agregar funcionalidad para representante si es necesario
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
