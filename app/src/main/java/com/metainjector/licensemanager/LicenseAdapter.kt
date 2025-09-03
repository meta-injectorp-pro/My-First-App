package com.metainjector.licensemanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView

class LicenseAdapter(
    private var licenseList: List<License>,
    private val onEditClicked: (License) -> Unit,
    private val onHistoryClicked: (License) -> Unit,
    private val onSendClicked: (License) -> Unit // নতুন Send বাটন এর জন্য লিসেনার
) : RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {

    class LicenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.textViewCustomerName)
        val email: TextView = itemView.findViewById(R.id.textViewEmail)
        val status: TextView = itemView.findViewById(R.id.textViewStatus)
        val pkg: TextView = itemView.findViewById(R.id.textViewPackage)
        val duration: TextView = itemView.findViewById(R.id.textViewDuration)
        val expiry: TextView = itemView.findViewById(R.id.textViewExpiry)
        val editButton: FrameLayout = itemView.findViewById(R.id.btnEdit)
        val historyButton: FrameLayout = itemView.findViewById(R.id.btnHistory) // Notun history button
        val sendButton: FrameLayout = itemView.findViewById(R.id.btnSend) // নতুন Send বাটন
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_license, parent, false)
        return LicenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return licenseList.size
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        val license = licenseList[position]
        holder.customerName.text = license.CustomerName ?: "N/A"
        holder.email.text = license.Email ?: "N/A"
        holder.status.text = license.Status ?: "N/A"
        holder.pkg.text = license.Package ?: "N/A"
        holder.expiry.text = license.ExpiryDate ?: "N/A"

        // --- নতুন সংশোধিত কোড ---
        val durationValue = license.Duration
        val packageType = license.Package

        holder.duration.text = when (packageType) {
            "Yearly" -> if (durationValue == "1") "1 Year" else "$durationValue Years"
            "Monthly" -> if (durationValue == "1") "1 Month" else "$durationValue Months"
            "Free Trial" -> "N/A"
            else -> durationValue ?: "N/A"
        }
        // --- নতুন কোড শেষ ---

        holder.editButton.setOnClickListener {
            onEditClicked(license)
        }
        holder.historyButton.setOnClickListener {
            onHistoryClicked(license)
        }
        holder.sendButton.setOnClickListener {
            onSendClicked(license)
        }

        val statusBackground = holder.status.background
        val wrappedDrawable = DrawableCompat.wrap(statusBackground)
        when (license.Status) {
            "Active" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#10B981"))
            "Expired" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#EF4444"))
            "Pending" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#F59E0B"))
            "Sent" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#007BFF"))
            "Disabled" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#6c757d"))
            "Free Trial" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#6B7280"))
            else -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#6B7280"))
        }
    }

    fun updateList(newList: List<License>) {
        licenseList = newList
        notifyDataSetChanged()
    }
}
