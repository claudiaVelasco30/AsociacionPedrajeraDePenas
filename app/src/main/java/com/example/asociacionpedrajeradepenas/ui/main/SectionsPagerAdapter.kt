package com.example.asociacionpedrajeradepenas.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.asociacionpedrajeradepenas.EventosAdminFragment
import com.example.asociacionpedrajeradepenas.R
import com.example.asociacionpedrajeradepenas.PenasAdminFragment
import com.example.asociacionpedrajeradepenas.InscripcionesAdminFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PenasAdminFragment()
            1 -> EventosAdminFragment()
            2 -> InscripcionesAdminFragment()
            else -> PenasAdminFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 3
    }
}