package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // Variables para autenticación con Firebase y Google One Tap
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        oneTapClient = Identity.getSignInClient(this)

        // Configurar el cuadro de diálogo para elegir cuenta
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true) // Habilita el inicio con ID token
                    .setServerClientId(getString(R.string.default_web_client_id)) // ID de cliente de Firebase
                    .setFilterByAuthorizedAccounts(false) // Permite seleccionar cualquier cuenta de Google
                    .build()
            ).build()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)

        btnIniciarSesion.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            var datosValidos = true

            if (email.isEmpty()) {
                etEmail.error = "El correo es obligatorio"
                datosValidos = false
            }

            if (password.isEmpty()) {
                etPassword.error = "La contraseña es obligatoria"
                datosValidos = false
            }

            if (datosValidos) {
                iniciarSesion(email, password, etEmail, etPassword)

            }
        }

        btnRegistro.setOnClickListener {
            // Navega a la pantalla de registro
            startActivity(Intent(this, RegistroActivity::class.java))
            finish()
        }

        btnGoogle.setOnClickListener {
            iniciarConGoogle()
        }
    }

    // Función para iniciar sesión con correo y contraseña
    private fun iniciarSesion(
        email: String,
        password: String,
        etEmail: EditText,
        etPassword: EditText
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si inicia sesión correctamente, navega a la pantalla principal
                startActivity(Intent(this, PantallaPrincipalActivity::class.java))
                etEmail.setText("")
                etPassword.setText("")
            } else {
                // Muestra error si no se pudo iniciar sesión
                Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para iniciar sesión con Google
    private fun iniciarConGoogle() {
        // Configura opciones de inicio de sesión con Google
        val googleConfig = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ID del cliente web de Firebase
            .requestEmail() // Solicita acceso al email del usuario
            .build()
        val clienteGoogle = GoogleSignIn.getClient(this, googleConfig)
        startActivityForResult(clienteGoogle.signInIntent, 100)
        clienteGoogle.signOut()
    }

    // Metodo que se llama cuando vuelve de la actividad de selección de cuenta de Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            val cuenta =
                task.getResult(ApiException::class.java) // Obtiene la cuenta seleccionada
            if (cuenta != null) {
                // Obtiene la credencial de Firebase a partir del ID Token de Google
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)

                // Inicia sesión con Firebase usando la credencial de Google
                auth.signInWithCredential(credencial).addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        val usuario = auth.currentUser
                        usuario?.let {
                            // Guarda los datos del usuario en Firestore
                            guardarUsuarioEnFirestore(
                                it.uid,
                                it.displayName ?: "Usuario",
                                it.email ?: ""
                            )
                        }
                        startActivity(Intent(this, PantallaPrincipalActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al iniciar sesión",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }

    // Guarda la información del usuario en Firestore si no existe
    private fun guardarUsuarioEnFirestore(uid: String, nombre: String, email: String) {
        val usuarios = database.collection("Usuarios").document(uid)

        // Verifica si el documento del usuario existe
        usuarios.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Si no existe, lo crea con los datos por defecto
                val usuario = hashMapOf(
                    "idUsuario" to uid,
                    "nombre" to nombre,
                    "email" to email,
                    "rol" to "usuario",
                    "idPeña" to null
                )
                usuarios.set(usuario)
            }
        }
    }
}