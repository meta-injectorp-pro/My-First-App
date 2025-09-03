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

class BugReportsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var bugReportAdapter: BugReportAdapter
    private val allBugReports = mutableListOf<BugReport>()

    private lateinit var rvBugReports: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bug_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        initializeViews(view)
        setupRecyclerView()
        setupListeners(view)
        fetchBugReports()
    }

    private fun initializeViews(view: View) {
        rvBugReports = view.findViewById(R.id.rvBugReports)
        progressBar = view.findViewById(R.id.progressBarBugs)
    }

    private fun setupRecyclerView() {
        rvBugReports.layoutManager = LinearLayoutManager(context)
        bugReportAdapter = BugReportAdapter(emptyList())
        rvBugReports.adapter = bugReportAdapter
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.btnRefreshBugs).setOnClickListener {
            fetchBugReports()
        }
        view.findViewById<Button>(R.id.btnDeleteBug).setOnClickListener {
            deleteSelectedBugReport()
        }
    }

    private fun fetchBugReports() {
        progressBar.visibility = View.VISIBLE
        rvBugReports.visibility = View.GONE

        db.collection("bugReports")
            .orderBy("Timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                allBugReports.clear()
                for (doc in documents) {
                    val report = doc.toObject(BugReport::class.java)
                    // Save document ID for deletion
                    report.docId = doc.id
                    allBugReports.add(report)
                }
                bugReportAdapter.updateList(allBugReports)
                progressBar.visibility = View.GONE
                rvBugReports.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load bug reports: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteSelectedBugReport() {
        val selectedReport = bugReportAdapter.getSelectedReport()

        if (selectedReport == null) {
            Toast.makeText(context, "Please select a bug report to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete the bug report from ${selectedReport.name}?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes, Delete") { _, _ ->
                db.collection("bugReports").document(selectedReport.docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Bug report deleted successfully.", Toast.LENGTH_SHORT).show()
                        fetchBugReports() // Refresh list after deletion
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error deleting report: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}