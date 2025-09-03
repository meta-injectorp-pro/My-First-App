package com.metainjector.licensemanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EditLicenseActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var currentLicense: License? = null

    // UI Elements
    private lateinit var etCustomerName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etLicenseKey: EditText
    private lateinit var etMachineId: EditText
    private lateinit var etActivationDate: EditText
    private lateinit var etExpiryDate: EditText
    private lateinit var etDuration: EditText
    private lateinit var spinnerPackage: Spinner
    private lateinit var spinnerStatus: Spinner

    // Extend Feature UI Elements
    private lateinit var btnMinus: Button
    private lateinit var etExtendValue: EditText
    private lateinit var btnPlus: Button
    private lateinit var spinnerExtendUnit: Spinner
    private lateinit var etNewSalePrice: EditText
    private lateinit var btnCalculate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_license)

        db = FirebaseFirestore.getInstance()
        initializeViews()
        setupExtendFeature()

        currentLicense = intent.getSerializableExtra("LICENSE_DATA") as? License

        if (currentLicense != null) {
            populateData(currentLicense!!)
        } else {
            Toast.makeText(this, "Error: Could not load license data.", Toast.LENGTH_LONG).show()
            finish()
        }

        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)
        btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        val btnDelete = findViewById<Button>(R.id.btnDelete)
        btnDelete.setOnClickListener {
            confirmAndDelete()
        }
    }

    private fun initializeViews() {
        etCustomerName = findViewById(R.id.etCustomerName)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etLicenseKey = findViewById(R.id.etLicenseKey)
        etMachineId = findViewById(R.id.etMachineId)
        etActivationDate = findViewById(R.id.etActivationDate)
        etExpiryDate = findViewById(R.id.etExpiryDate)
        etDuration = findViewById(R.id.etDuration)
        spinnerPackage = findViewById(R.id.spinnerPackage)
        spinnerStatus = findViewById(R.id.spinnerStatus)

        btnMinus = findViewById(R.id.btnMinus)
        etExtendValue = findViewById(R.id.etExtendValue)
        btnPlus = findViewById(R.id.btnPlus)
        spinnerExtendUnit = findViewById(R.id.spinnerExtendUnit)
        etNewSalePrice = findViewById(R.id.etNewSalePrice)
        btnCalculate = findViewById(R.id.btnCalculate)
    }

    private fun populateData(license: License) {
        etCustomerName.setText(license.CustomerName)
        etEmail.setText(license.Email)
        etPhoneNumber.setText(license.PhoneNumber)
        etLicenseKey.setText(license.id)
        etMachineId.setText(license.MachineID)
        etActivationDate.setText(license.ActivationDate)
        etExpiryDate.setText(license.ExpiryDate)
        etDuration.setText(license.Duration)

        setSpinnerSelection(spinnerPackage, R.array.package_options, license.Package)
        setSpinnerSelection(spinnerStatus, R.array.status_options, license.Status)

        spinnerPackage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPackage = parent?.getItemAtPosition(position).toString()
                val newDuration = when (selectedPackage) {
                    "Monthly" -> "1"
                    "Yearly" -> "1" // <-- পরিবর্তন এখানে
                    "Free Trial" -> "1"
                    else -> etDuration.text.toString()
                }
                etDuration.setText(newDuration)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, arrayId: Int, value: String?) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val position = adapter.getPosition(value)
        if (position >= 0) {
            spinner.setSelection(position)
        }
    }

    private fun setupExtendFeature() {
        val units = arrayOf("Day", "Month", "Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExtendUnit.adapter = adapter
        spinnerExtendUnit.setSelection(1)

        btnMinus.setOnClickListener {
            var value = etExtendValue.text.toString().toIntOrNull() ?: 0
            if (value > 0) value--
            etExtendValue.setText(value.toString())
        }
        btnPlus.setOnClickListener {
            var value = etExtendValue.text.toString().toIntOrNull() ?: 0
            value++
            etExtendValue.setText(value.toString())
        }
        btnCalculate.setOnClickListener {
            val currentValue = etExtendValue.text.toString().toIntOrNull() ?: 0
            if (currentValue > 0) {
                val newDate = calculateNewExpiryDate(
                    etExpiryDate.text.toString(),
                    currentValue,
                    spinnerExtendUnit.selectedItem.toString()
                )
                etExpiryDate.setText(newDate)
                Toast.makeText(this, "Expiry date updated. Press 'Save Changes' to confirm.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun calculateNewExpiryDate(currentDateStr: String, amount: Int, unit: String): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        try {
            val currentDate = dateFormat.parse(currentDateStr) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = currentDate

            when (unit) {
                "Day" -> calendar.add(Calendar.DAY_OF_MONTH, amount)
                "Month" -> calendar.add(Calendar.MONTH, amount)
                "Year" -> calendar.add(Calendar.YEAR, amount)
            }
            return dateFormat.format(calendar.time)
        } catch (e: Exception) {
            Log.e("DateCalc", "Error calculating new date", e)
            Toast.makeText(this, "Invalid current date format.", Toast.LENGTH_SHORT).show()
            return currentDateStr
        }
    }

    private fun saveChanges() {
        val licenseId = currentLicense?.id ?: return
        val newSalePrice = etNewSalePrice.text.toString().trim()

        if (newSalePrice.isNotEmpty()) {
            val price = newSalePrice.toDoubleOrNull()
            if (price == null || price <= 0) {
                Toast.makeText(this, "Please enter a valid sale price.", Toast.LENGTH_SHORT).show()
                return
            }

            val salesRecord = hashMapOf(
                "Timestamp" to Date(),
                "License Key" to licenseId,
                "Package" to spinnerPackage.selectedItem.toString(),
                "Final Price" to newSalePrice
            )

            db.collection("salesLogs").add(salesRecord)
                .addOnSuccessListener {
                    Log.d("EditLicense", "New sales log created for renewal.")
                    updateLicenseDocument(licenseId)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error creating sales log: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            updateLicenseDocument(licenseId)
        }
    }

    private fun updateLicenseDocument(licenseId: String) {
        val updatedData = mapOf(
            "Customer Name" to etCustomerName.text.toString(),
            "Email" to etEmail.text.toString(),
            "Phone Number" to etPhoneNumber.text.toString(),
            "MachineID" to etMachineId.text.toString(),
            "Activation Date" to etActivationDate.text.toString(),
            "Expiry Date" to etExpiryDate.text.toString(),
            "Package" to spinnerPackage.selectedItem.toString(),
            "Duration" to etDuration.text.toString(),
            "Status" to spinnerStatus.selectedItem.toString()
        )

        db.collection("licenseDatabase").document(licenseId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "License updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating license: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this license? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteLicense()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteLicense() {
        val licenseId = currentLicense?.id ?: return
        db.collection("licenseDatabase").document(licenseId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "License deleted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting license: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}