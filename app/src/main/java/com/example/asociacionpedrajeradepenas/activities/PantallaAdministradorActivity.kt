package com.example.asociacionpedrajeradepenas.activities

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.example.asociacionpedrajeradepenas.fragments.SectionsPagerAdapter
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaAdministradorBinding

class PantallaAdministradorActivity : BaseActivity() {

    private lateinit var binding: ActivityPantallaAdministradorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuramos la barra de herramientas reutilizando el metodo definido en BaseActivity
        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)

        // Creamos un adaptador para manejar los fragments que se mostrarán en las pestañas
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        // Enlazamos el ViewPager con el adaptador para que se muestren los fragments al deslizar
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter

        // Configuramos las pestañas para que estén sincronizadas con el ViewPager
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

    }
}