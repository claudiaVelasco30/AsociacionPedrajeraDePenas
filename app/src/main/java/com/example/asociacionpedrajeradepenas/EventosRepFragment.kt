package com.example.asociacionpedrajeradepenas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asociacionpedrajeradepenas.databinding.FragmentEventosRepBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventosRepFragment : Fragment() {

    private var _binding: FragmentEventosRepBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var eventoAdapter: EventosRepAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventosRepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rveventosrep.layoutManager = LinearLayoutManager(requireContext())

        // Carga los eventos desde Firestore
        cargarEventos()
    }

    // Función que carga los eventos desde Firestore y filtra los eventos ya pasados
    private fun cargarEventos() {
        db.collection("Eventos").get()
            .addOnSuccessListener { result ->
                val listaEventos = mutableListOf<Map<String, Any>>()

                // Formato para comparar fechas sin horas
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Obtener la fecha actual sin hora
                val fechaActual = sdf.parse(sdf.format(Date()))

                for (document in result) {
                    val evento = document.data.toMutableMap()
                    evento["idEvento"] = document.id

                    // Extrae la fecha del evento como Timestamp
                    val timestamp = evento["fecha_hora"] as? Timestamp

                    // Filtra los eventos que aún no han ocurrido
                    if (timestamp != null) {
                        val fechaEvento = sdf.parse(sdf.format(timestamp.toDate()))
                        if (fechaEvento != null && !fechaEvento.before(fechaActual)) {
                            listaEventos.add(evento)
                        }
                    }
                }

                eventoAdapter = EventosRepAdapter(listaEventos)
                binding.rveventosrep.adapter = eventoAdapter
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}