package com.metainjector.licensemanager

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

class FirestoreListenerService : Service() {

    private val tag = "FirestoreListener"
    private val foregroundNotificationId = 1

    private val listeners = mutableListOf<ListenerRegistration>()
    private var isServiceRunning = false

    // --- নতুন কোড: প্রতিটি লিসেনারের জন্য আলাদা ফ্ল্যাগ ---
    private var isInitialFormLoad = true
    private var isInitialBugLoad = true
    private var isInitialSecurityLoad = true
    private var isInitialLicenseLoad = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(foregroundNotificationId, createForegroundNotification())
        Log.d(tag, "Foreground service started.")

        if (!isServiceRunning) {
            setupListeners()
            isServiceRunning = true
        }
        return START_STICKY
    }

    private fun setupListeners() {
        val db = FirebaseFirestore.getInstance()
        Log.d(tag, "Setting up Firestore listeners...")

        // Listener for new Form Responses
        val formListener = db.collection("purchaseForm").addSnapshotListener { snapshots, e ->
            if (e != null) { return@addSnapshotListener }
            // শুধুমাত্র সত্যিকারের নতুন ডেটার জন্য নোটিফিকেশন
            if (isInitialFormLoad) {
                isInitialFormLoad = false
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val userName = dc.document.getString("Your Full Name") ?: "a user"
                    showDataChangeNotification("New Form Response", "New form submitted by $userName.", 2)
                }
            }
        }
        listeners.add(formListener)

        // Listener for new Bug Reports
        val bugListener = db.collection("bugReports").addSnapshotListener { snapshots, e ->
            if (e != null) { return@addSnapshotListener }
            if (isInitialBugLoad) {
                isInitialBugLoad = false
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val userName = dc.document.getString("Name") ?: "a user"
                    showDataChangeNotification("New Bug Report", "A new bug has been reported by $userName.", 3)
                }
            }
        }
        listeners.add(bugListener)

        // Listener for new Security Logs
        val securityLogListener = db.collection("securityLogs").addSnapshotListener { snapshots, e ->
            if (e != null) { return@addSnapshotListener }
            if (isInitialSecurityLoad) {
                isInitialSecurityLoad = false
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val userEmail = dc.document.getString("email") ?: "Unknown"
                    showDataChangeNotification("New Security Log", "A new security event occurred for $userEmail.", 4)
                }
            }
        }
        listeners.add(securityLogListener)

        // Listener for License add & Status Changes
        val licenseStatusListener = db.collection("licenseDatabase").addSnapshotListener { snapshots, e ->
            if (e != null) { return@addSnapshotListener }
            // প্রথম লোড উপেক্ষা করা হচ্ছে, শুধুমাত্র পরের পরিবর্তনগুলো দেখা হবে
            if (isInitialLicenseLoad) {
                isInitialLicenseLoad = false
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                val license = dc.document.toObject(License::class.java)
                if (dc.type == DocumentChange.Type.ADDED) {
                    if (license.Status == "Pending" && license.Email.isNullOrBlank()) {
                        showDataChangeNotification("New Pending License", "A new license key has been added.", 5)
                    }
                }
                if (dc.type == DocumentChange.Type.MODIFIED) {
                    val userName = license.CustomerName ?: "a user"
                    val newStatus = license.Status
                    if (newStatus == "Active" || newStatus == "Expired") {
                        showDataChangeNotification("License Status Updated", "$userName's license is now $newStatus.", 6)
                    }
                }
            }
        }
        listeners.add(licenseStatusListener)

        Log.d(tag, "All listeners have been attached.")
    }

    private fun showDataChangeNotification(title: String, body: String, notificationId: Int) {
        val notification = NotificationCompat.Builder(this, MyApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, MyApp.CHANNEL_ID)
            .setContentTitle("License Manager Active")
            .setContentText("Listening for database changes in the background.")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        listeners.forEach { it.remove() }
        isServiceRunning = false
        Log.d(tag, "Firestore listeners stopped and service destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    // --- নতুন কোডটি এখানে যোগ করুন ---
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Stop the service when the user swipes the app from recent apps
        stopSelf()
    }
}