package com.metainjector.licensemanager

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Locale
import kotlin.random.Random

class PasswordRecoveryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var emailSender: EmailSender
    private var recoveryCode: String = ""
    private var adminEmail: String = "" // Store admin's recovery email
    private var adminUsername: String = "" // Store username for password update

    // Views
    private lateinit var layoutSendCode: LinearLayout
    private lateinit var layoutResetPassword: LinearLayout
    private lateinit var etRecoveryUsername: EditText
    private lateinit var btnSendCode: Button
    private lateinit var tvInfo: TextView
    private lateinit var etVerificationCode: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery)

        db = FirebaseFirestore.getInstance()
        emailSender = EmailSender()
        initializeViews()

        // Pre-fill username from LoginActivity if available
        val usernameFromIntent = intent.getStringExtra("USERNAME")
        if (!usernameFromIntent.isNullOrEmpty()) {
            etRecoveryUsername.setText(usernameFromIntent)
        }

        btnSendCode.setOnClickListener { handleSendCode() }
        btnResetPassword.setOnClickListener { handleResetPassword() }
    }

    private fun initializeViews() {
        layoutSendCode = findViewById(R.id.layoutSendCode)
        layoutResetPassword = findViewById(R.id.layoutResetPassword)
        etRecoveryUsername = findViewById(R.id.etRecoveryUsername)
        btnSendCode = findViewById(R.id.btnSendCode)
        tvInfo = findViewById(R.id.tvInfo)
        etVerificationCode = findViewById(R.id.etVerificationCode)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        progressBar = findViewById(R.id.progressBarRecovery)
    }

    private fun handleSendCode() {
        adminUsername = etRecoveryUsername.text.toString().trim()
        if (adminUsername.isEmpty()) {
            etRecoveryUsername.error = "Username cannot be empty"
            return
        }
        setLoading(true)

        // Fetch admin details from Firestore
        db.collection("AdminUsers").document(adminUsername).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    adminEmail = document.getString("email") ?: ""
                    if (adminEmail.isNotEmpty()) {
                        sendRecoveryEmail()
                    } else {
                        showError("Recovery email not found for this user.")
                    }
                } else {
                    showError("Admin username not found.")
                }
            }
            .addOnFailureListener { e ->
                showError("Error fetching user data: ${e.message}")
            }
    }

    private fun sendRecoveryEmail() {
        recoveryCode = Random.nextInt(100000, 999999).toString()
        val subject = "Your Password Recovery Code"
        val body = "Your password recovery code is: <b>$recoveryCode</b>"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                emailSender.sendEmail(adminEmail, subject, body)
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@PasswordRecoveryActivity, "Recovery code sent to your email.", Toast.LENGTH_LONG).show()
                    // Switch to reset password layout
                    layoutSendCode.visibility = View.GONE
                    layoutResetPassword.visibility = View.VISIBLE
                    tvInfo.text = "A 6-digit code has been sent to $adminEmail. Please enter the code and your new password below."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to send email: ${e.message}")
                }
            }
        }
    }

    private fun handleResetPassword() {
        val code = etVerificationCode.text.toString().trim()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (code != recoveryCode) {
            etVerificationCode.error = "Incorrect code"
            return
        }
        if (newPassword.isEmpty() || newPassword.length < 6) {
            etNewPassword.error = "Password must be at least 6 characters"
            return
        }
        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return
        }
        setLoading(true)

        val newPasswordHash = hashString(newPassword)

        db.collection("AdminUsers").document(adminUsername)
            .update("password_hash", newPasswordHash)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_LONG).show()
                finish() // Go back to LoginActivity
            }
            .addOnFailureListener { e ->
                showError("Failed to update password: ${e.message}")
            }
    }

    // SHA-256 Hashing function (same as desktop app)
    private fun hashString(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnSendCode.isEnabled = false
            btnResetPassword.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnSendCode.isEnabled = true
            btnResetPassword.isEnabled = true
        }
    }

    private fun showError(message: String) {
        setLoading(false)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}