package com.example.asociacionpedrajeradepenas.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Patterns
import com.example.asociacionpedrajeradepenas.R

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellidos = findViewById<EditText>(R.id.etApellidos)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPassword2 = findViewById<EditText>(R.id.etPassword2)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)

        btnCrearCuenta.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val password2 = etPassword2.text.toString().trim()
            var esValido = true

            // Limpiar errores anteriores
            etNombre.error = null
            etApellidos.error = null
            etEmail.error = null
            etPassword.error = null
            etPassword2.error = null

            // Validaciones de campos
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es obligatorio"
                esValido = false
            }

            if (apellidos.isEmpty()) {
                etApellidos.error = "Los apellidos son obligatorios"
                esValido = false
            }

            if (email.isEmpty()) {
                etEmail.error = "El correo es obligatorio"
                esValido = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Formato de correo no válido"
                esValido = false
            }

            if (password.isEmpty()) {
                etPassword.error = "La contraseña es obligatoria"
                esValido = false
            } else if (password.length < 6) {
                etPassword.error = "Debe tener al menos 6 caracteres"
                esValido = false
            }

            if (password2.isEmpty()) {
                etPassword2.error = "Repite la contraseña"
                esValido = false
            } else if (password != password2) {
                etPassword2.error = "Las contraseñas no coinciden"
                esValido = false
            }

            // Si todo está correcto se crea la cuenta
            if (esValido) {
                registerUser(nombre, apellidos, email, password)
            }
        }
    }

    // Función para registrar el usuario en Firebase
    private fun registerUser(nombre: String, apellidos: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val usuario = auth.currentUser
                    if (usuario != null) {
                        val idUsuario = usuario.uid
                        guardarUsuarioEnFirestore(idUsuario, nombre, apellidos, email)
                    }
                } else {
                    Toast.makeText(this, "Error al registrarse", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Función para guardar los datos del usuario en Firestore
    private fun guardarUsuarioEnFirestore(
        idUsuario: String,
        nombre: String,
        apellidos: String,
        email: String
    ) {
        val usuario = hashMapOf(
            "idUsuario" to idUsuario,
            "nombre" to nombre,
            "apellidos" to apellidos,
            "email" to email,
            "rol" to "usuario",
            "idPeña" to null
        )

        // Guardar en la colección "Usuarios"
        db.collection("Usuarios").document(idUsuario).set(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PantallaPrincipalActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrarse", Toast.LENGTH_SHORT).show()
            }
    }
}