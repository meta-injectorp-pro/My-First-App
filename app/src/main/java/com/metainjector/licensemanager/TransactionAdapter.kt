package com.metainjector.licensemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactionList: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvTransactionDate)
        val pkg: TextView = itemView.findViewById(R.id.tvTransactionPackage)
        val price: TextView = itemView.findViewById(R.id.tvTransactionPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        // Timestamp theke date format kora hocche
        holder.date.text = transaction.Timestamp?.toDate()?.let {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
        } ?: "N/A"

        holder.pkg.text = transaction.Package ?: "N/A"
        holder.price.text = "${transaction.FinalPrice ?: "0"} BDT"
    }

    override fun getItemCount(): Int = transactionList.size
}
