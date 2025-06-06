package com.example.asociacionpedrajeradepenas.fragments

import android.content.Intent
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.asociacionpedrajeradepenas.R
import com.example.asociacionpedrajeradepenas.activities.DetallePenaActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
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

        // Comprobamos si se ha pasado una peña desde DetallePenaActivity
        val idPenaMostrar = activity?.intent?.getStringExtra("idPenaMostrar")
        if (!idPenaMostrar.isNullOrEmpty()) {
            mostrarInfoPeña(idPenaMostrar)
        }
    }

    override fun onMapReady(googlemap: GoogleMap) {
        // Inicializa el mapa cuando está listo y configura los controles
        mapa = googlemap
        mapa.uiSettings.isMapToolbarEnabled = true

        // Centra el mapa en la ubicación del pueblo Pedrajas de San Esteban y establece los límites
        val centroMapa = LatLng(41.342386, -4.582162)
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(centroMapa, 15f))

        val limitesMapa = LatLngBounds (LatLng(41.335043, -4.591619), LatLng(41.347696, -4.576101))
        mapa.setLatLngBoundsForCameraTarget(limitesMapa)

        mapa.setMinZoomPreference(15f)

        // Carga los marcadores de todas las peñas desde Firebase
        cargarPeñas()

        // Oculta la información de la peña si se hace click fuera del marcador
        mapa.setOnMapClickListener {
            val contenedorInfo =
                requireActivity().findViewById<FrameLayout>(R.id.contenedorInfoPena)
            contenedorInfo.removeAllViews()
        }

        // Muestra la info de la peña si se hace click en un marcador
        mapa.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            val idPeña = marker.tag as? String
            if (idPeña != null) {
                mostrarInfoPeña(idPeña)
            }
            false
        }
    }

    private fun cargarPeñas() {
        // Consulta todas las peñas en Firebase
        db.collection("Penas").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val nombre = document.getString("nombre") ?: "Peña sin nombre"
                val idPeña = document.id

                val latitudStr = document.getString("lat") ?: "0.0"
                val longitudStr = document.getString("lon") ?: "0.0"

                val latitud = latitudStr.toDoubleOrNull() ?: 0.0
                val longitud = longitudStr.toDoubleOrNull() ?: 0.0

                if (latitud != 0.0 || longitud != 0.0) {
                    val ubicacion = LatLng(latitud, longitud)

                    val marcador = mapa.addMarker(
                        MarkerOptions().position(ubicacion).title(nombre)
                    )

                    // Asociar el id de la peña al marker usando su tag
                    marcador?.tag = idPeña
                }

            }
        }
    }

    private fun mostrarInfoPeña(idPeña: String) {
        // Muestra la vista con información de la peña en la parte inferior
        val contenedorInfo = requireActivity().findViewById<FrameLayout>(R.id.contenedorInfoPena)
        contenedorInfo.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        val itemView = inflater.inflate(R.layout.item_pena, contenedorInfo, false)

        val imgPena = itemView.findViewById<ImageView>(R.id.imgPena)
        val tvNombre = itemView.findViewById<TextView>(R.id.tvNombrePena)
        val btnMasInfo = itemView.findViewById<Button>(R.id.btnMasInfo)
        val btnUnirse = itemView.findViewById<Button>(R.id.btnUnirse)

        db.collection("Penas").document(idPeña).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val pena = mapOf(
                    "id" to idPeña,
                    "nombre" to document.getString("nombre"),
                    "ubicación" to document.getString("ubicacion"),
                    "idRepresentante" to document.getString("idRepresentante"),
                    "imagen" to document.getString("imagen")
                )

                tvNombre.text = pena["nombre"] as String? ?: "Peña sin nombre"

                val imagenUrl = pena["imagen"] as? String
                if (!imagenUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imagenUrl).into(imgPena)
                }

                btnMasInfo.setOnClickListener {
                    abrirDetallePena(pena)
                }

                btnUnirse.setOnClickListener {
                    mostrarDialogoUnirse(pena)
                }

                contenedorInfo.addView(itemView)
                contenedorInfo.visibility = View.VISIBLE
            }
        }
    }

    private fun abrirDetallePena(pena: Map<String, Any?>) {
        val intent = Intent(requireContext(), DetallePenaActivity::class.java).apply {
            putExtra("idPeña", pena["id"] as? String)
            putExtra("nombre", pena["nombre"] as? String)
            putExtra("ubicacion", pena["ubicación"] as? String)
            putExtra("idRepresentante", pena["idRepresentante"] as? String)
            putExtra("imagen", pena["imagen"] as? String)
        }
        startActivity(intent)
    }

    // Muestra un AlertDialog personalizado para confirmar la unión a la peña
    private fun mostrarDialogoUnirse(pena: Map<String, String?>) {
        val nombrePena = pena["nombre"].toString()
        val idPena = pena["idPeña"].toString()

        val dialogo = layoutInflater.inflate(R.layout.dialogo_principal, null)

        val titulo = dialogo.findViewById<TextView>(R.id.tituloDialogo)
        val mensaje = dialogo.findViewById<TextView>(R.id.mensajeDialogo)
        val btnAceptar = dialogo.findViewById<TextView>(R.id.btnAceptar)
        val btnCancelar = dialogo.findViewById<TextView>(R.id.btnCancelar)

        titulo.text = "Unirse a peña"
        mensaje.text = "¿Deseas realizar una solicitud para unirte a la peña \"$nombrePena\"?"

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogo)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        btnCancelar.setOnClickListener {
            alertDialog.dismiss()
        }

        btnAceptar.setOnClickListener {
            val idUsuario = FirebaseAuth.getInstance().currentUser?.uid
            if (idUsuario != null) {
                crearSolicitud(idPena, idUsuario)
            }
            alertDialog.dismiss()
        }
    }

    // Crea un documento en la colección 'Solicitudes' con los datos del usuario y la peña
    private fun crearSolicitud(idPena: String, idUsuario: String) {
        val database = FirebaseFirestore.getInstance()
        val idSolicitud = database.collection("Solicitudes").document().id

        val solicitud = hashMapOf(
            "idSolicitud" to idSolicitud,
            "idPena" to idPena,
            "idUsuario" to idUsuario,
            "estado" to "pendiente"
        )

        db.collection("Solicitudes").add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al enviar solicitud", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}