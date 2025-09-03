package com.metainjector.licensemanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView

class FilteredUserAdapter(private val userList: List<License>) :
    RecyclerView.Adapter<FilteredUserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val userEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val expiryDate: TextView = itemView.findViewById(R.id.tvExpiryDate)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_filtered_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.CustomerName ?: "N/A"
        holder.userEmail.text = user.Email ?: "N/A"
        holder.expiryDate.text = "Expires on: ${user.ExpiryDate ?: "N/A"}"
        holder.status.text = user.Status ?: "N/A"

        val statusBackground = holder.status.background
        val wrappedDrawable = DrawableCompat.wrap(statusBackground)
        when (user.Status) {
            "Active" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#10B981"))
            "Expired" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#EF4444"))
            "Pending" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#F59E0B"))
            "Sent" -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#007BFF"))
            else -> DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#6B7280"))
        }
    }

    override fun getItemCount() = userList.size
}