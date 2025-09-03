package com.metainjector.licensemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class KeyItem(val key: String, var isSelected: Boolean = false)

class KeyAdapter(private val keyList: MutableList<KeyItem>) : RecyclerView.Adapter<KeyAdapter.KeyViewHolder>() {

    class KeyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.cbKey)
        val keyText: TextView = itemView.findViewById(R.id.tvKey)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_key, parent, false)
        return KeyViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
        val item = keyList[position]
        holder.keyText.text = item.key
        holder.checkBox.isChecked = item.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
        }
    }

    override fun getItemCount(): Int = keyList.size

    fun getSelectedKeys(): List<String> {
        return keyList.filter { it.isSelected }.map { it.key }
    }

    fun selectAll(isSelected: Boolean) {
        keyList.forEach { it.isSelected = isSelected }
        notifyDataSetChanged()
    }
}
