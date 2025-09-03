package com.metainjector.licensemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SecurityLogsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var securityLogAdapter: SecurityLogAdapter
    private val allSecurityLogs = mutableListOf<SecurityLog>()

    private lateinit var rvSecurityLogs: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_security_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        initializeViews(view)
        setupRecyclerView()
        setupListeners(view)
        fetchSecurityLogs()
    }

    private fun initializeViews(view: View) {
        rvSecurityLogs = view.findViewById(R.id.rvSecurityLogs)
        progressBar = view.findViewById(R.id.progressBarSecurity)
    }

    private fun setupRecyclerView() {
        rvSecurityLogs.layoutManager = LinearLayoutManager(context)
        securityLogAdapter = SecurityLogAdapter(emptyList())
        rvSecurityLogs.adapter = securityLogAdapter
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.btnRefreshSecurityLogs).setOnClickListener {
            fetchSecurityLogs()
        }
        view.findViewById<Button>(R.id.btnDeleteSecurityLog).setOnClickListener {
            deleteSelectedLog()
        }
    }

    private fun fetchSecurityLogs() {
        progressBar.visibility = View.VISIBLE
        rvSecurityLogs.visibility = View.GONE

        db.collection("securityLogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                allSecurityLogs.clear()
                for (doc in documents) {
                    val log = doc.toObject(SecurityLog::class.java)
                    log.docId = doc.id
                    allSecurityLogs.add(log)
                }
                securityLogAdapter.updateList(allSecurityLogs)
                progressBar.visibility = View.GONE
                rvSecurityLogs.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load security logs: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteSelectedLog() {
        val selectedLog = securityLogAdapter.getSelectedLog()

        if (selectedLog == null) {
            Toast.makeText(context, "Please select a log to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete the log for ${selectedLog.email}?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes, Delete") { _, _ ->
                db.collection("securityLogs").document(selectedLog.docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Log deleted successfully.", Toast.LENGTH_SHORT).show()
                        fetchSecurityLogs() // Refresh list
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error deleting log: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}