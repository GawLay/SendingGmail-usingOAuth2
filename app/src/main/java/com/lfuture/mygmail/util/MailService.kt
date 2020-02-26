package com.lfuture.mygmail.util

import android.util.Log
import com.google.api.client.util.Base64
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class MailService {
    @Throws(MessagingException::class, IOException::class)
    fun sendMessage(
        service: Gmail,
        userId: String,
        email: MimeMessage
    ): String {
        var message = createMessageWithEmail(email)
        // GMail's official method to send email with oauth2.0
        message = service.users().messages().send("me", message).execute()
        println(message.toPrettyString());
        return message.id
    }

    // Method to create email Params
    @Throws(MessagingException::class)
    fun createEmail(
        to: String,
        from: String,
        subject: String,
        bodyText: String
    ): MimeMessage {
        val props = Properties()
        val session = Session.getDefaultInstance(props, null)

        val email = MimeMessage(session)
        val tAddress = InternetAddress(to)
        val fAddress = InternetAddress(from)

        email.setFrom(fAddress)
        email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress)
        email.subject = subject

        // Create Multipart object and add MimeBodyPart objects to this object
        val multipart = MimeMultipart()

        // Changed for adding attachment and text
        email.setText(bodyText);

        val textBody = MimeBodyPart()
        textBody.setText(bodyText)
        multipart.addBodyPart(textBody)

        //enable if you want to send attachment too

        //filename = the name of file you want to upload get from Gallery or file etc
//        if (fileName != "") {
//            // Create new MimeBodyPart object and set DataHandler object to this object
//            val attachmentBody = MimeBodyPart()
//            val filename = fileName // change accordingly
//            val source = FileDataSource(filename)
//            attachmentBody.dataHandler = DataHandler(source)
//            attachmentBody.fileName = filename
//            multipart.addBodyPart(attachmentBody)
//        }

        //Set the multipart object to the message object
        email.setContent(multipart)
        Log.e("Message", "Create EMail")
        return email
    }

    @Throws(MessagingException::class, IOException::class)
    private fun createMessageWithEmail(email: MimeMessage): Message {
        val bytes = ByteArrayOutputStream()
        email.writeTo(bytes)
        val encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray())
        val message = Message()
        message.raw = encodedEmail
        Log.e("Message", "Create MessageWithEmail")
        return message
    }


}