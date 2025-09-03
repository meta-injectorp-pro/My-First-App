package com.metainjector.licensemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class FilteredUserListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtered_user_list)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rvFilteredUsers)
        toolbar = findViewById(R.id.toolbarUserList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val filterType = intent.getStringExtra("FILTER_TYPE")
        supportActionBar?.title = "$filterType Users"

        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchAndFilterUsers(filterType)
    }

    private fun fetchAndFilterUsers(filterType: String?) {
        if (filterType == null) return

        db.collection("licenseDatabase").get().addOnSuccessListener { documents ->
            val allLicenses = documents.toObjects(License::class.java)

            val filteredList = when (filterType) {
                "Total" -> allLicenses.filter { !it.Email.isNullOrBlank() }
                "Pending" -> allLicenses.filter { !it.Email.isNullOrBlank() && it.Status == "Pending" }
                else -> allLicenses.filter { it.Status == filterType }
            }

            recyclerView.adapter = FilteredUserAdapter(filteredList)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}