package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.example.asociacionpedrajeradepenas.databinding.ActivityPantallaAdministradorBinding

class PantallaAdministradorActivity : BaseActivity() {

    private lateinit var binding: ActivityPantallaAdministradorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbar, binding.nombreToolbar, binding.iconoUsuario)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

    }
}