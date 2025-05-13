package com.example.asociacionpedrajeradepenas.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Clase base que proporciona funcionalidad común para otras actividades
open class BaseActivity : AppCompatActivity() {

    protected lateinit var auth: FirebaseAuth
    protected lateinit var db: FirebaseFirestore
    protected var userRole: String = "usuario"
    protected var idPenaUsuario: String? = null
    protected var nombrePenaUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtiene el rol del usuario para controlar el menú
        obtenerRolUsuario()
    }

    protected fun setupToolbar(
        toolbar: Toolbar,
        nombreToolbar: TextView,
        imagenToolbar: ShapeableImageView
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Oculta el título por defecto

        // Ajustar el margen superior para evitar que se solape con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight - 8, 0, 8)
            WindowInsetsCompat.CONSUMED
        }

        // Muestra el nombre del usuario y la imagen de la peña en la toolbar
        obtenerNombreImagen(nombreToolbar, imagenToolbar)

    }

    // Metodo que obtiene el nombre del usuario y la imagen de su peña
    private fun obtenerNombreImagen(nombreToolbar: TextView, imagenToolbar: ShapeableImageView) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreUsuario = document.getString("nombre") ?: "Nombre"
                        idPenaUsuario = document.getString("idPeña")
                        nombreToolbar.text = nombreUsuario

                        if (!idPenaUsuario.isNullOrEmpty()) {
                            db.collection("Penas").document(idPenaUsuario!!)
                                .get()
                                .addOnSuccessListener { penaDoc ->
                                    if (penaDoc.exists()) {
                                        nombrePenaUsuario = penaDoc.getString("nombre")
                                        val urlImagen = penaDoc.getString("imagen")
                                        if (!urlImagen.isNullOrEmpty()) {
                                            Glide.with(imagenToolbar.context)
                                                .load(urlImagen)
                                                .into(imagenToolbar)
                                        }
                                    }
                                }
                        }
                    }
                }
        }
    }

    // Metodo para obtener el rol del usuario desde la base de datos
    protected fun obtenerRolUsuario() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userRole = document.getString("rol") ?: "usuario"
                        idPenaUsuario = document.getString("idPeña")
                        invalidateOptionsMenu() // Refrescar menú tras obtener el rol
                    }
                }
        }
    }

    // Crea el menú de la toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        // Muestra el botón de opciones solo si el usuario es administrador o representante
        val isAdminOrRep = userRole == "administrador" || userRole == "representante"
        menu?.findItem(R.id.action_opciones)?.isVisible = isAdminOrRep

        // Muestra el botón de abandonar solo si el usuario pertenece a una peña
        val isUsuarioConPena = userRole == "usuario" && !idPenaUsuario.isNullOrEmpty()
        menu?.findItem(R.id.action_abandonar)?.isVisible = isUsuarioConPena

        return true
    }

    // Maneja las acciones del menú de la toolbar
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
                    startActivity(Intent(this, PantallaRepresentanteActivity::class.java))
                }
                true
            }

            R.id.action_abandonar -> {
                mostrarDialogoAbandonar()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarDialogoAbandonar() {
        val dialogo = layoutInflater.inflate(R.layout.dialogo_principal, null)

        val titulo = dialogo.findViewById<TextView>(R.id.tituloDialogo)
        val mensaje = dialogo.findViewById<TextView>(R.id.mensajeDialogo)
        val btnAceptar = dialogo.findViewById<TextView>(R.id.btnAceptar)
        val btnCancelar = dialogo.findViewById<TextView>(R.id.btnCancelar)

        titulo.text = "Abandonar peña"
        mensaje.text = "¿Estás seguro de que quieres abandonar la peña \"$nombrePenaUsuario\"?"

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogo)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        btnCancelar.setOnClickListener {
            alertDialog.dismiss()
        }

        btnAceptar.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("Usuarios").document(userId)
                    .update("idPeña", null)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Has abandonado la peña", Toast.LENGTH_SHORT).show()
                        idPenaUsuario = null
                        invalidateOptionsMenu()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al abandonar la peña", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }
}

