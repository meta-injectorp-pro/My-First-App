package com.metainjector.licensemanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SecurityLogAdapter(
    private var logList: List<SecurityLog>
) : RecyclerView.Adapter<SecurityLogAdapter.LogViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val email: TextView = itemView.findViewById(R.id.tvLogEmail)
        val date: TextView = itemView.findViewById(R.id.tvLogDate)
        val eventType: TextView = itemView.findViewById(R.id.tvLogEventType)
        val machineId: TextView = itemView.findViewById(R.id.tvLogMachineId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_security_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]

        holder.email.text = log.email ?: "N/A"
        holder.eventType.text = "Event: ${log.eventType ?: "N/A"}"
        holder.machineId.text = "Machine ID: ${log.machineId ?: "N/A"}"

        holder.date.text = log.timestamp?.toDate()?.let {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it)
        } ?: "N/A"

        // Handle item selection
        holder.itemView.setBackgroundColor(if (selectedPosition == position) Color.parseColor("#CFD8DC") else Color.TRANSPARENT)
        holder.itemView.setOnClickListener {
            selectedPosition = if (selectedPosition == position) RecyclerView.NO_POSITION else position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = logList.size

    fun updateList(newList: List<SecurityLog>) {
        logList = newList
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun getSelectedLog(): SecurityLog? {
        return if (selectedPosition != RecyclerView.NO_POSITION && logList.isNotEmpty()) {
            logList[selectedPosition]
        } else {
            null
        }
    }
}