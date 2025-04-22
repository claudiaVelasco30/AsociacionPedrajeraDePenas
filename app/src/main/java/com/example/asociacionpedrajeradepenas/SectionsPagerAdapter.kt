package com.example.asociacionpedrajeradepenas

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// Array que contiene los identificadores de los títulos de las pestañas
private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    // Devuelve el fragment correspondiente a la posición indicada
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PenasAdminFragment()
            1 -> EventosAdminFragment()
            2 -> InscripcionesAdminFragment()
            else -> PenasAdminFragment()
        }
    }

    // Devuelve el título de la pestaña correspondiente
    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    //Devuelve el número total de pestañas que maneja el adaptador
    override fun getCount(): Int {
        return 3
    }
}