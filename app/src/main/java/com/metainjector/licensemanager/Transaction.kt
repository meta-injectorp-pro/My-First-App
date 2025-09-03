package com.metainjector.licensemanager

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Transaction(
    @get:PropertyName("Timestamp") @set:PropertyName("Timestamp")
    var Timestamp: Timestamp? = null,

    @get:PropertyName("Package") @set:PropertyName("Package")
    var Package: String? = null,

    @get:PropertyName("Final Price") @set:PropertyName("Final Price")
    var FinalPrice: Any? = null, // String ba Number, dutoi accept korbe

    @get:PropertyName("License Key") @set:PropertyName("License Key")
    var LicenseKey: String? = null // Notun ei line ti add kora hoyeche
) {
    constructor() : this(null, null, null, null)
}
