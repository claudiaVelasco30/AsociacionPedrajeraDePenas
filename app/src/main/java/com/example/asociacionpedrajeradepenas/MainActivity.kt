package com.example.asociacionpedrajeradepenas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(this)

        // Configurar el cuadro de diálogo para elegir cuenta
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id)) // ID de cliente de Firebase
                    .setFilterByAuthorizedAccounts(false) // Permite seleccionar cualquier cuenta de Google
                    .build()
            )
            .build()

        val etEmail = findViewById<EditText>(R.id.editTextEmail)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)

        btnIniciarSesion.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
            finish()
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, PantallaPrincipalActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(
            BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false) // Permite elegir cualquier cuenta o agregar una nueva
                        .build()
                )
                .setAutoSelectEnabled(true) // Permite agregar una cuenta si no hay ninguna vinculada
                .build()
        ).addOnSuccessListener { result ->
            googleSignInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
        }.addOnFailureListener {
            Toast.makeText(this, "Error con Google: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, PantallaPrincipalActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al obtener credenciales", Toast.LENGTH_SHORT).show()
            }
        }
    }
}