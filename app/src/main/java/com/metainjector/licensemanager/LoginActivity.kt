package com.metainjector.licensemanager

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and start MainActivity if so.
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnTogglePassword = findViewById<ImageButton>(R.id.btn_toggle_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_login)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnTogglePassword.setImageResource(R.drawable.ic_eye_slashed)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnTogglePassword.setImageResource(R.drawable.ic_eye)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etUsername.error = "Email is required"
                etUsername.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        goToMainActivity()
                    } else {
                        Toast.makeText(this, "Incorrect login details", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, PasswordRecoveryActivity::class.java)
            intent.putExtra("USERNAME", etUsername.text.toString().trim())
            startActivity(intent)
        }
    }

    private fun goToMainActivity() {
        // Start the background listener service using the recommended method for all Android versions.
        val serviceIntent = Intent(this, FirestoreListenerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish() // Finish LoginActivity
    }
}