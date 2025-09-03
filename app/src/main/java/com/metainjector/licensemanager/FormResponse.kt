package com.metainjector.licensemanager

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class FormResponse(
    var docId: String = "",

    @get:PropertyName("Timestamp") @set:PropertyName("Timestamp")
    var Timestamp: Timestamp? = null,

    @get:PropertyName("Your Full Name") @set:PropertyName("Your Full Name")
    var FullName: String? = null,

    @get:PropertyName("Email") @set:PropertyName("Email")
    var Email: String? = null,

    @get:PropertyName("Phone Number") @set:PropertyName("Phone Number")
    var PhoneNumber: String? = null,

    @get:PropertyName("Select Your Package") @set:PropertyName("Select Your Package")
    var Package: String? = null,

    @get:PropertyName("Payment Method") @set:PropertyName("Payment Method")
    var PaymentMethod: String? = null,

    // This property accepts ANYTHING (String or Number) from Firestore for Amount
    @get:PropertyName("Amount Sent (BDT)")
    @set:PropertyName("Amount Sent (BDT)")
    var amountSentRaw: Any? = null,

    // This property accepts ANYTHING for TrxID and has the correct two trailing spaces
    @get:PropertyName("Sender's Number or TrxID  ")
    @set:PropertyName("Sender's Number or TrxID  ")
    var transactionIdRaw: Any? = null,

    // This property has the correct two trailing spaces
    @get:PropertyName("Upload Payment Screenshot  ")
    @set:PropertyName("Upload Payment Screenshot  ")
    var ScreenshotUrl: String? = null
) {
    // This makes AmountSent always available as a Long? to the app
    @get:Exclude
    val AmountSent: Long?
        get() {
            return when (amountSentRaw) {
                is Long -> amountSentRaw as Long
                is String -> (amountSentRaw as String).toLongOrNull()
                else -> null
            }
        }

    // This makes TransactionId always available as a String? to the app
    @get:Exclude
    val TransactionId: String?
        get() {
            return transactionIdRaw?.toString()
        }

    // Default constructor needed for Firestore
    constructor() : this("", null, null, null, null, null, null, null, null, null)
}