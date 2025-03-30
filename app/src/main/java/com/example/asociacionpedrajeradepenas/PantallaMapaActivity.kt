package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PantallaMapaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_mapa)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val nombreToolbar = findViewById<TextView>(R.id.nombreToolbar)

        // Eliminar título de la Toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorMapa, MapaFragment())
            .commit()
    }
}