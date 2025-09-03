package com.metainjector.licensemanager

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class BugReport(
    // This field is for our use, not stored in Firestore document fields
    var docId: String = "",

    @get:PropertyName("Timestamp") @set:PropertyName("Timestamp")
    var timestamp: Timestamp? = null,

    @get:PropertyName("Version") @set:PropertyName("Version")
    var version: String? = null,

    @get:PropertyName("Name") @set:PropertyName("Name")
    var name: String? = null,

    @get:PropertyName("Email") @set:PropertyName("Email")
    var email: String? = null,

    @get:PropertyName("Description") @set:PropertyName("Description")
    var description: String? = null,

    @get:PropertyName("Screenshot Link") @set:PropertyName("Screenshot Link")
    var screenshotLink: String? = null
) {
    // Fix: Added an empty string "" for docId to match the primary constructor
    constructor() : this("", null, null, null, null, null, null)
}