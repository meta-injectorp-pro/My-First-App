package com.metainjector.licensemanager

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BugReportAdapter(
    private var reportList: List<BugReport>
) : RecyclerView.Adapter<BugReportAdapter.BugReportViewHolder>() {

    // This will keep track of the selected item's position
    private var selectedPosition = RecyclerView.NO_POSITION

    class BugReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvBugReportName)
        val date: TextView = itemView.findViewById(R.id.tvBugReportDate)
        val version: TextView = itemView.findViewById(R.id.tvBugReportVersion)
        val description: TextView = itemView.findViewById(R.id.tvBugReportDescription)
        val viewScreenshotBtn: Button = itemView.findViewById(R.id.btnViewBugScreenshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BugReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_bug_report, parent, false)
        return BugReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: BugReportViewHolder, position: Int) {
        val report = reportList[position]

        // Set data to the views
        holder.name.text = report.name ?: "N/A"
        holder.version.text = "Version: ${report.version ?: "N/A"}"
        holder.description.text = report.description ?: "No description provided."

        holder.date.text = report.timestamp?.toDate()?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
        } ?: "N/A"

        // Screenshot button click listener
        holder.viewScreenshotBtn.setOnClickListener {
            val url = report.screenshotLink
            if (!url.isNullOrBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(holder.itemView.context, "Could not open link.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(holder.itemView.context, "No screenshot URL found.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Selection Logic for Deletion ---
        // Highlight the item if it's selected
        holder.itemView.setBackgroundColor(if (selectedPosition == position) Color.parseColor("#FFCDD2") else Color.TRANSPARENT)

        // Handle item click to select/deselect
        holder.itemView.setOnClickListener {
            if (selectedPosition == position) {
                selectedPosition = RecyclerView.NO_POSITION // Deselect if already selected
            } else {
                selectedPosition = position // Select new item
            }
            notifyDataSetChanged() // Redraw the list to show selection changes
        }
    }

    override fun getItemCount(): Int = reportList.size

    // Method to update the list with new data
    fun updateList(newList: List<BugReport>) {
        reportList = newList
        selectedPosition = RecyclerView.NO_POSITION // Reset selection when list is refreshed
        notifyDataSetChanged()
    }

    // Method for the Fragment to get the currently selected report
    fun getSelectedReport(): BugReport? {
        return if (selectedPosition != RecyclerView.NO_POSITION && reportList.isNotEmpty()) {
            reportList[selectedPosition]
        } else {
            null
        }
    }
}