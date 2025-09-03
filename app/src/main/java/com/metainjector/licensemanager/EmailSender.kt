package com.metainjector.licensemanager

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {

    // নিচের দুটি জায়গায় আপনার নিজের তথ্য দিন
    private val senderEmail = "app.metainjectorpro@gmail.com" // আপনার জিমেইল অ্যাড্রেস
    private val appPassword = "lsrx ppyy rykg uqxi" // আপনার ১৬ ডিজিটের অ্যাপ পাসওয়ার্ড

    private val properties: Properties = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
    }

    private val session: Session = Session.getInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(senderEmail, appPassword)
        }
    })

    fun sendEmail(recipientEmail: String, subject: String, body: String) {
        try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                this.subject = subject
                // HTML content পাঠানোর জন্য
                setContent(body, "text/html; charset=utf-8")
            }
            Transport.send(mimeMessage)
            println("Email sent successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // re-throw the exception to be caught by the caller
        }
    }
}