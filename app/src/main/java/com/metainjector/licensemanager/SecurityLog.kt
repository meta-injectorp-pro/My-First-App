package com.metainjector.licensemanager

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class SecurityLog(
    var docId: String = "", // For deletion

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Timestamp? = null,

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String? = null,

    @get:PropertyName("event_type") @set:PropertyName("event_type")
    var eventType: String? = null,

    @get:PropertyName("machine_id") @set:PropertyName("machine_id")
    var machineId: String? = null
) {
    // Default constructor for Firestore
    constructor() : this("", null, null, null, null)
}