package com.metainjector.licensemanager

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class KeyGeneratorFragment : Fragment() {

    private lateinit var etNumberOfKeys: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnClear: Button
    private lateinit var tvGeneratedKeys: TextView
    private lateinit var btnAddKeysToDb: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var db: FirebaseFirestore
    private var generatedKeys = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_key_generator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        initializeViews(view)
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        etNumberOfKeys = view.findViewById(R.id.etNumberOfKeys)
        btnGenerate = view.findViewById(R.id.btnGenerate)
        btnClear = view.findViewById(R.id.btnClear)
        tvGeneratedKeys = view.findViewById(R.id.tvGeneratedKeys)
        btnAddKeysToDb = view.findViewById(R.id.btnAddKeysToDb)
        progressBar = view.findViewById(R.id.progressBarAddKeys)
        tvGeneratedKeys.movementMethod = ScrollingMovementMethod()
    }

    private fun setupClickListeners() {
        btnGenerate.setOnClickListener { generateKeys() }
        btnClear.setOnClickListener { clearFields() }
        btnAddKeysToDb.setOnClickListener { addKeysToDatabase() }
    }

    private fun generateKeys() {
        val numKeysStr = etNumberOfKeys.text.toString()
        if (numKeysStr.isEmpty()) {
            Toast.makeText(context, "Please enter the number of keys to generate.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val numKeys = numKeysStr.toInt()
            if (numKeys <= 0) {
                Toast.makeText(context, "Please enter a positive number.", Toast.LENGTH_SHORT).show()
                return
            }

            generatedKeys = List(numKeys) { UUID.randomUUID().toString().uppercase() }
            tvGeneratedKeys.text = generatedKeys.joinToString("\n")
            Toast.makeText(context, "$numKeys keys generated.", Toast.LENGTH_SHORT).show()

        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Invalid number format.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        etNumberOfKeys.text.clear()
        tvGeneratedKeys.text = ""
        generatedKeys = listOf()
    }

    private fun addKeysToDatabase() {
        if (generatedKeys.isEmpty()) {
            Toast.makeText(context, "Please generate keys first.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnAddKeysToDb.isEnabled = false

        val batch = db.batch()
        generatedKeys.forEach { key ->
            val docRef = db.collection("licenseDatabase").document(key)
            val data = mapOf("Email" to "", "Status" to "Pending")
            batch.set(docRef, data)
        }

        batch.commit()
            .addOnSuccessListener {
                progressBar.visibility = View.INVISIBLE
                btnAddKeysToDb.isEnabled = true
                Toast.makeText(context, "${generatedKeys.size} keys successfully added to database.", Toast.LENGTH_LONG).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.INVISIBLE
                btnAddKeysToDb.isEnabled = true
                Toast.makeText(context, "Error adding keys: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
