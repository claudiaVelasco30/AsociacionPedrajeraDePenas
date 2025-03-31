package com.example.asociacionpedrajeradepenas

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapa: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googlemap: GoogleMap) {
        mapa = googlemap
        val centroMapa = LatLng(41.342386, -4.582162)
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(centroMapa, 15f))

        cargarPeñas()
    }

    private fun cargarPeñas() {
        db.collection("Penas").get().addOnSuccessListener { documents ->
            for (document in documents) {
                try {
                    val nombre = document.getString("nombre") ?: "Peña sin nombre"

                    val latitudStr = document.getString("lat") ?: "0.0"
                    val longitudStr = document.getString("lon") ?: "0.0"

                    val latitud = latitudStr.toDoubleOrNull() ?: 0.0
                    val longitud = longitudStr.toDoubleOrNull() ?: 0.0

                    if(latitud != 0.0 || longitud != 0.0){
                        val ubicacion = LatLng(latitud, longitud)
                        // Agregar marcador al mapa
                        mapa.addMarker(MarkerOptions().position(ubicacion).title(nombre))
                    }
                } catch (e: Exception) {
                    Log.e("FirestoreError", "Error procesando documento: ${document.id}", e)
                }

            }
        }
    }
}