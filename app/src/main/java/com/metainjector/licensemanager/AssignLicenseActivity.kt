package com.metainjector.licensemanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AssignLicenseActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // UI Elements
    private lateinit var etLicenseKey: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var spinnerPackage: Spinner
    private lateinit var spinnerDuration: Spinner
    private lateinit var etSalePrice: EditText
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_license)

        db = FirebaseFirestore.getInstance()
        initializeViews()
        setupListeners()

        val keyToAssign = intent.getStringExtra("KEY_TO_ASSIGN")
        if (keyToAssign.isNullOrEmpty() || keyToAssign == "N/A") {
            Toast.makeText(this, "No available key to assign.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        etLicenseKey.setText(keyToAssign)
    }

    private fun initializeViews() {
        etLicenseKey = findViewById(R.id.etAssignLicenseKey)
        etCustomerName = findViewById(R.id.etAssignCustomerName)
        etEmail = findViewById(R.id.etAssignEmail)
        etPhoneNumber = findViewById(R.id.etAssignPhoneNumber)
        spinnerPackage = findViewById(R.id.spinnerAssignPackage)
        spinnerDuration = findViewById(R.id.spinnerAssignDuration)
        etSalePrice = findViewById(R.id.etAssignSalePrice)
        btnConfirm = findViewById(R.id.btnConfirmAssignment)
    }

    private fun setupListeners() {
        spinnerPackage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPackage = parent?.getItemAtPosition(position).toString()
                val durationAdapter = spinnerDuration.adapter as? ArrayAdapter<String>
                if (durationAdapter != null) {
                    val newDurationPosition = when (selectedPackage) {
                        "Monthly" -> durationAdapter.getPosition("1")
                        "Yearly" -> durationAdapter.getPosition("1")
                        "Free Trial" -> durationAdapter.getPosition("1")
                        else -> -1
                    }
                    if (newDurationPosition != -1) {
                        spinnerDuration.setSelection(newDurationPosition)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnConfirm.setOnClickListener {
            assignLicense()
        }
    }

    private fun assignLicense() {
        val keyToAssign = etLicenseKey.text.toString()
        val customerName = etCustomerName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhoneNumber.text.toString().trim()
        val pkg = spinnerPackage.selectedItem.toString()
        val duration = spinnerDuration.selectedItem.toString()
        val price = etSalePrice.text.toString().trim()

        if (customerName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Customer Name and Email are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val activationDate = sdf.format(Date())

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        when (pkg) {
            "Monthly" -> calendar.add(Calendar.MONTH, duration.toIntOrNull() ?: 1)
            "Yearly" -> calendar.add(Calendar.YEAR, duration.toIntOrNull() ?: 1)
            "Free Trial" -> calendar.add(Calendar.MONTH, 1)
        }
        val expiryDate = sdf.format(calendar.time)

        val updateData = mapOf(
            "Customer Name" to customerName,
            "Email" to email,
            "Phone Number" to phone,
            "Package" to pkg,
            "Duration" to duration,
            "Status" to if (pkg == "Free Trial") "Free Trial" else "Active",
            "Activation Date" to activationDate,
            "Expiry Date" to expiryDate
        )

        db.collection("licenseDatabase").document(keyToAssign)
            .update(updateData)
            .addOnSuccessListener {
                if (price.isNotEmpty() && price.toDoubleOrNull() ?: 0.0 > 0) {
                    val salesRecord = hashMapOf(
                        "Timestamp" to Date(),
                        "License Key" to keyToAssign,
                        "Package" to pkg,
                        "Final Price" to price
                    )
                    db.collection("salesLogs").add(salesRecord)
                }
                Toast.makeText(this, "License assigned successfully.", Toast.LENGTH_SHORT).show()
                finish() // Close this activity and go back to the list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error assigning license: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}