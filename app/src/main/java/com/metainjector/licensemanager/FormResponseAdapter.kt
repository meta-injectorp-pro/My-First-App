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

class FormResponseAdapter(
    private var responseList: List<FormResponse>
) : RecyclerView.Adapter<FormResponseAdapter.ResponseViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    // ViewHolder class to hold the views for each item
    class ResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find all the views from the updated layout
        val name: TextView = itemView.findViewById(R.id.tvResponseName)
        val date: TextView = itemView.findViewById(R.id.tvResponseDate)
        val email: TextView = itemView.findViewById(R.id.tvResponseEmail)
        val phone: TextView = itemView.findViewById(R.id.tvResponsePhone)
        val pkg: TextView = itemView.findViewById(R.id.tvResponsePackage)
        val amount: TextView = itemView.findViewById(R.id.tvResponseAmount)
        val method: TextView = itemView.findViewById(R.id.tvResponsePaymentMethod)
        val trxId: TextView = itemView.findViewById(R.id.tvResponseTrxId)
        val viewScreenshotBtn: Button = itemView.findViewById(R.id.btnViewScreenshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_form_response, parent, false)
        return ResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        val response = responseList[position]

        // Set data to the views
        holder.name.text = response.FullName ?: "N/A"
        holder.email.text = response.Email ?: "N/A"
        holder.phone.text = "Phone: ${response.PhoneNumber ?: "N/A"}"
        holder.pkg.text = "Package: ${response.Package ?: "N/A"}"
        holder.amount.text = "Amount: ${response.AmountSent?.toString() ?: "N/A"} BDT"
        holder.method.text = "Method: ${response.PaymentMethod ?: "N/A"}"
        holder.trxId.text = "TrxID: ${response.TransactionId ?: "N/A"}"

        // Format and set the date
        holder.date.text = response.Timestamp?.toDate()?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
        } ?: "N/A"

        // Screenshot button click listener
        holder.viewScreenshotBtn.setOnClickListener {
            val url = response.ScreenshotUrl
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

        // Handle item selection for deletion
        holder.itemView.setBackgroundColor(if (selectedPosition == position) Color.parseColor("#E0E7FF") else Color.TRANSPARENT)
        holder.itemView.setOnClickListener {
            if (selectedPosition == position) {
                selectedPosition = RecyclerView.NO_POSITION // Deselect
            } else {
                selectedPosition = position // Select
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = responseList.size

    fun updateList(newList: List<FormResponse>) {
        responseList = newList
        selectedPosition = RecyclerView.NO_POSITION // Reset selection on new list
        notifyDataSetChanged()
    }

    // Method to get the selected response for deletion
    fun getSelectedResponse(): FormResponse? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            responseList[selectedPosition]
        } else {
            null
        }
    }
}