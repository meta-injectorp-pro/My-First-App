package com.metainjector.licensemanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LicenseListFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var licenseAdapter: LicenseAdapter
    private val allAssignedLicenses = mutableListOf<License>()
    private val allAvailableKeys = mutableListOf<String>()
    private var licenseListener: ListenerRegistration? = null

    // UI elements
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var spinnerStatusFilter: Spinner
    private lateinit var tvAvailableKeys: TextView
    private lateinit var tvNextKey: TextView
    private lateinit var btnManageKeys: Button
    private lateinit var btnGoToAssign: Button

    private val TAG = "LicenseListFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_license_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        initializeViews(view)
        setupRecyclerView()
        setupSearchAndFilter()
        setupActionButtons()
    }

    override fun onStart() {
        super.onStart()
        setupRealtimeLicenseListener()
    }

    override fun onStop() {
        super.onStop()
        licenseListener?.remove()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewLicenses)
        progressBar = view.findViewById(R.id.progressBarList)
        etSearch = view.findViewById(R.id.etSearch)
        spinnerStatusFilter = view.findViewById(R.id.spinnerStatusFilter)
        tvAvailableKeys = view.findViewById(R.id.tvAvailableKeys)
        tvNextKey = view.findViewById(R.id.tvNextKey)
        btnManageKeys = view.findViewById(R.id.btnManageKeys)
        btnGoToAssign = view.findViewById(R.id.btnGoToAssign)
    }

    private fun setupRecyclerView() {
        licenseAdapter = LicenseAdapter(
            licenseList = emptyList(),
            onEditClicked = { license ->
                val intent = Intent(activity, EditLicenseActivity::class.java)
                intent.putExtra("LICENSE_DATA", license)
                startActivity(intent)
            },
            onHistoryClicked = { license -> showTransactionHistoryDialog(license) },
            onSendClicked = { license -> showSendEmailDialog(license) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = licenseAdapter
    }

    private fun setupSearchAndFilter() {
        val statusOptions = listOf("Default") + resources.getStringArray(R.array.status_options).toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatusFilter.adapter = adapter

        spinnerStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterLicenses()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterLicenses() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupActionButtons() {
        btnManageKeys.setOnClickListener {
            showManageKeysDialog()
        }
        btnGoToAssign.setOnClickListener {
            val intent = Intent(activity, AssignLicenseActivity::class.java)
            intent.putExtra("KEY_TO_ASSIGN", tvNextKey.text.toString())
            startActivity(intent)
        }
    }

    private fun setupRealtimeLicenseListener() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        licenseListener = db.collection("licenseDatabase")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    allAssignedLicenses.clear()
                    allAvailableKeys.clear()
                    for (document in snapshots) {
                        val license = document.toObject(License::class.java).copy(id = document.id)
                        if (license.Email.isNullOrBlank()) {
                            allAvailableKeys.add(license.id)
                        } else {
                            allAssignedLicenses.add(license)
                        }
                    }
                    filterLicenses()
                    tvAvailableKeys.text = allAvailableKeys.size.toString()
                    tvNextKey.text = allAvailableKeys.firstOrNull() ?: "N/A"
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
    }

    private fun filterLicenses() {
        val searchText = etSearch.text.toString().lowercase(Locale.getDefault())
        val selectedStatus = spinnerStatusFilter.selectedItem.toString()

        val filteredList = allAssignedLicenses.filter { license ->
            val statusMatch = selectedStatus == "Default" || license.Status == selectedStatus

            val searchMatch = if (searchText.isEmpty()) {
                true
            } else {
                license.CustomerName?.lowercase(Locale.getDefault())?.contains(searchText) == true ||
                        license.Email?.lowercase(Locale.getDefault())?.contains(searchText) == true ||
                        license.id.lowercase(Locale.getDefault()).contains(searchText) ||
                        license.PhoneNumber?.contains(searchText) == true ||
                        license.MachineID?.lowercase(Locale.getDefault())?.contains(searchText) == true
            }

            statusMatch && searchMatch
        }
        licenseAdapter.updateList(filteredList)
    }

    private fun showManageKeysDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_manage_keys, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val rvKeys = dialogView.findViewById<RecyclerView>(R.id.rvKeys)
        val cbSelectAll = dialogView.findViewById<CheckBox>(R.id.cbSelectAll)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        val keyItems = allAvailableKeys.map { KeyItem(it) }.toMutableList()
        val keyAdapter = KeyAdapter(keyItems)

        rvKeys.layoutManager = LinearLayoutManager(context)
        rvKeys.adapter = keyAdapter

        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            keyAdapter.selectAll(isChecked)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            val selectedKeys = keyAdapter.getSelectedKeys()
            if (selectedKeys.isEmpty()) {
                Toast.makeText(context, "Please select at least one key to delete.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete ${selectedKeys.size} key(s)? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    deleteKeysFromFirestore(selectedKeys, dialog)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        dialog.show()
    }

    private fun deleteKeysFromFirestore(keysToDelete: List<String>, manageDialog: AlertDialog) {
        val batch = db.batch()
        keysToDelete.forEach { key ->
            val docRef = db.collection("licenseDatabase").document(key)
            batch.delete(docRef)
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(context, "${keysToDelete.size} keys deleted successfully.", Toast.LENGTH_SHORT).show()
            manageDialog.dismiss()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error deleting keys: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error in batch delete", e)
        }
    }

    private fun showTransactionHistoryDialog(license: License) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaction_history, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val title = dialogView.findViewById<TextView>(R.id.tvHistoryTitle)
        val rvHistory = dialogView.findViewById<RecyclerView>(R.id.rvHistory)
        val btnClose = dialogView.findViewById<Button>(R.id.btnClose)

        title.text = "History for ${license.CustomerName}"
        rvHistory.layoutManager = LinearLayoutManager(context)

        db.collection("salesLogs")
            .orderBy("Timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "No transaction history found.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@addOnSuccessListener
                }

                val allTransactions = documents.toObjects(Transaction::class.java)
                val userTransactions = allTransactions.filter { it.LicenseKey == license.id }

                if (userTransactions.isEmpty()) {
                    Toast.makeText(context, "No transaction history found for this user.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@addOnSuccessListener
                }

                rvHistory.adapter = TransactionAdapter(userTransactions)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load history: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "History fetch error", e)
            }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSendEmailDialog(license: License) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_send_email, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val tvUserName = dialogView.findViewById<TextView>(R.id.tvDialogUserName)
        val tvUserEmail = dialogView.findViewById<TextView>(R.id.tvDialogUserEmail)
        val btnSendBoth = dialogView.findViewById<Button>(R.id.btnSendBoth)
        val btnSendSoftware = dialogView.findViewById<Button>(R.id.btnSendSoftware)
        val btnSendKey = dialogView.findViewById<Button>(R.id.btnSendKey)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val statusLayout = dialogView.findViewById<View>(R.id.layoutStatus)
        val statusText = dialogView.findViewById<TextView>(R.id.tvSendStatus)

        val customerName = license.CustomerName ?: "Valued User"
        val userEmail = license.Email ?: ""
        val licenseKey = license.id

        tvUserName.text = "To: $customerName"
        tvUserEmail.text = "Email: $userEmail"

        fun send(mailType: String) {
            if (userEmail.isEmpty()) {
                Toast.makeText(context, "Recipient email address is not available.", Toast.LENGTH_LONG).show()
                return
            }

            btnSendBoth.isEnabled = false
            btnSendSoftware.isEnabled = false
            btnSendKey.isEnabled = false
            btnCancel.isEnabled = false
            statusLayout.visibility = View.VISIBLE

            val subject: String
            val body: String
            val fileDownloadUrl = "https://www.dropbox.com/scl/fi/qt611a2v2k43cg77fo5cu/Meta-Injector-Pro.zip?rlkey=5j2brhtsam79faxlqqvhcpnfv&st=bj1tgyu5&dl=1"
            val baseStyle = "font-family: Arial, sans-serif; color: #333; line-height: 1.6;"
            val buttonStyle = "background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; display: inline-block;"
            val keyStyle = "background-color: #f2f2f2; border: 1px solid #ddd; padding: 10px; font-family: monospace; font-size: 14px; border-radius: 4px;"

            when (mailType) {
                "both" -> {
                    statusText.text = "Sending Key + Software..."
                    subject = "Your License and Software for Meta Injector Pro"
                    body = """<div style="$baseStyle"><p>Hello $customerName,</p><p>Thank you for choosing Meta Injector Pro. We are excited to have you with us.</p><p>Here is your license key to activate the software:</p><p style="$keyStyle">$licenseKey</p><p>You can download the latest version of the software using the button below:</p><p><a href="$fileDownloadUrl" style="$buttonStyle">Download Software</a></p><br><p>Best regards,<br>The Meta Injector Pro Team</p></div>"""
                }
                "software" -> {
                    statusText.text = "Sending Software Only..."
                    subject = "Your Meta Injector Pro Software Download Link"
                    body = """<div style="$baseStyle"><p>Hello $customerName,</p><p>As requested, you can download the latest version of the software using the button below:</p><p><a href="$fileDownloadUrl" style="$buttonStyle">Download Software</a></p><br><p>Best regards,<br>The Meta Injector Pro Team</p></div>"""
                }
                else -> { // key
                    statusText.text = "Sending Key Only..."
                    subject = "Your Meta Injector Pro License Key"
                    body = """<div style="$baseStyle"><p>Hello $customerName,</p><p>As requested, here is your license key for Meta Injector Pro.</p><p>Your License Key:</p><p style="$keyStyle">$licenseKey</p><br><p>Best regards,<br>The Meta Injector Pro Team</p></div>"""
                }
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    EmailSender().sendEmail(userEmail, subject, body)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Email sent successfully to $userEmail", Toast.LENGTH_LONG).show()

                        if (mailType == "both" && license.Status == "Pending") {
                            db.collection("licenseDatabase").document(license.id)
                                .update("Status", "Sent")
                                .addOnSuccessListener {
                                    Log.d(TAG, "Status updated to Sent for user: ${license.Email}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to update status for user: ${license.Email}", e)
                                }
                        }

                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to send email: ${e.message}", Toast.LENGTH_LONG).show()
                        btnSendBoth.isEnabled = true
                        btnSendSoftware.isEnabled = true
                        btnSendKey.isEnabled = true
                        btnCancel.isEnabled = true
                        statusLayout.visibility = View.GONE
                    }
                }
            }
        }

        btnSendBoth.setOnClickListener { send("both") }
        btnSendSoftware.setOnClickListener { send("software") }
        btnSendKey.setOnClickListener { send("key") }
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}