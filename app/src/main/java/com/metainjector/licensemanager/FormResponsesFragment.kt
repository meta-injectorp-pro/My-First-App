package com.metainjector.licensemanager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class FormResponsesFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var responseAdapter: FormResponseAdapter
    private val allResponses = mutableListOf<FormResponse>()

    private lateinit var etSearch: EditText
    private lateinit var rvResponses: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_form_responses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        initializeViews(view)
        setupRecyclerView()
        setupListeners(view)
        fetchResponses()
    }

    private fun initializeViews(view: View) {
        etSearch = view.findViewById(R.id.etSearchResponses)
        rvResponses = view.findViewById(R.id.rvFormResponses)
        progressBar = view.findViewById(R.id.progressBarResponses)
    }

    private fun setupRecyclerView() {
        rvResponses.layoutManager = LinearLayoutManager(context)
        responseAdapter = FormResponseAdapter(emptyList())
        rvResponses.adapter = responseAdapter
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.btnRefreshResponses).setOnClickListener { fetchResponses() }

        // --- UPDATED DELETE BUTTON LISTENER ---
        view.findViewById<Button>(R.id.btnDeleteResponse).setOnClickListener {
            deleteSelectedResponse()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterResponses(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchResponses() {
        progressBar.visibility = View.VISIBLE
        rvResponses.visibility = View.GONE

        db.collection("purchaseForm")
            .orderBy("Timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                allResponses.clear()
                for (doc in documents) {
                    val response = doc.toObject(FormResponse::class.java)
                    response.docId = doc.id
                    allResponses.add(response)
                }
                responseAdapter.updateList(allResponses)
                progressBar.visibility = View.GONE
                rvResponses.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load responses: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterResponses(query: String) {
        val filteredList = if (query.isEmpty()) {
            allResponses
        } else {
            allResponses.filter {
                it.FullName?.contains(query, true) == true ||
                        it.Email?.contains(query, true) == true ||
                        it.PhoneNumber?.contains(query, true) == true ||
                        it.TransactionId?.contains(query, true) == true
            }
        }
        responseAdapter.updateList(filteredList)
    }

    // --- NEW FUNCTION FOR DELETION ---
    private fun deleteSelectedResponse() {
        val selectedResponse = responseAdapter.getSelectedResponse()

        if (selectedResponse == null) {
            Toast.makeText(context, "Please select a response to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to permanently delete the response from ${selectedResponse.FullName}?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes, Delete") { _, _ ->
                // User confirmed, proceed with deletion
                db.collection("purchaseForm").document(selectedResponse.docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Response deleted successfully.", Toast.LENGTH_SHORT).show()
                        fetchResponses() // Refresh the list from server
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error deleting response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}