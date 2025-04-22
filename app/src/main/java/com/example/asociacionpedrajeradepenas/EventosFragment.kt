package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.databinding.FragmentEventosBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventosFragment : Fragment() {

    private var _binding: FragmentEventosBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var eventoAdapter: EventoAdapter

    // Se infla la vista del fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Se ejecuta cuando la vista ya está creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())

        // Carga los eventos desde Firestore
        cargarEventos()
    }

    // Función para cargar los eventos desde la colección "Eventos"
    private fun cargarEventos() {
        db.collection("Eventos").get()
            .addOnSuccessListener { result ->
                val listaEventos =
                    mutableListOf<Map<String, Any>>() // Lista para almacenar eventos válidos
                val sdf = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ) // Formato para comparar fechas

                // Se obtene la fecha actual sin hora
                val fechaActual = sdf.parse(sdf.format(Date()))

                for (document in result) {
                    val evento = document.data
                    val timestamp = evento["fecha_hora"] as? Timestamp

                    if (timestamp != null) {
                        // Formateo de la fecha del evento para eliminar la hora
                        val fechaEvento = sdf.parse(sdf.format(timestamp.toDate()))

                        // Se agregan los eventos cuya fecha sea igual o posterior a la fecha actual
                        if (fechaEvento != null && !fechaEvento.before(fechaActual)) {
                            listaEventos.add(evento)
                        }
                    }
                }

                // Crear y asignar el adaptador con los eventos
                eventoAdapter = EventoAdapter(listaEventos)
                binding.rvEventos.adapter = eventoAdapter
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}