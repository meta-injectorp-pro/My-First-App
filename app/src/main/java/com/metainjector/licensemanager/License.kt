package com.metainjector.licensemanager

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

// Serializable implement kora hoyeche jate ei object ti activity'r moddhe pass kora jay
data class License(
    var id: String = "",

    @get:PropertyName("Customer Name") @set:PropertyName("Customer Name")
    var CustomerName: String? = null,

    @get:PropertyName("Email") @set:PropertyName("Email")
    var Email: String? = null,

    @get:PropertyName("Status") @set:PropertyName("Status")
    var Status: String? = null,

    @get:PropertyName("MachineID") @set:PropertyName("MachineID")
    var MachineID: String? = null,

    @get:PropertyName("Activation Date") @set:PropertyName("Activation Date")
    var ActivationDate: String? = null,

    @get:PropertyName("Expiry Date") @set:PropertyName("Expiry Date")
    var ExpiryDate: String? = null,

    @get:PropertyName("Duration") @set:PropertyName("Duration")
    var Duration: String? = null,

    @get:PropertyName("Package") @set:PropertyName("Package")
    var Package: String? = null,

    @get:PropertyName("Phone Number") @set:PropertyName("Phone Number")
    var PhoneNumber: String? = null
) : Serializable { // <-- Ei line ti add kora hoyeche
    // Firestore er jonno ekta khali constructor proyojon.
    constructor() : this("", null, null, null, null, null, null, null, null, null)
}
