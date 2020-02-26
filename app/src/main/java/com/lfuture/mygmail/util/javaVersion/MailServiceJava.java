package com.lfuture.mygmail.util.javaVersion;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailServiceJava {
    // Method to send email
    public String sendMessage(Gmail service,
                              String userId,
                              MimeMessage email)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(email);
        // GMail's official method to send email with oauth2.0
        message = service.users().messages().send(userId, message).execute();

        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
        return message.getId();
    }

    // Method to create email Params
    public MimeMessage createEmail(String to,
                                   String from,
                                   String subject,
                                   String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);

        email.setFrom(fAddress);
        email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
        email.setSubject(subject);

        // Create Multipart object and add MimeBodyPart objects to this object
        Multipart multipart = new MimeMultipart();

        // Changed for adding attachment and text
        // email.setText(bodyText);

        BodyPart textBody = new MimeBodyPart();
        textBody.setText(bodyText);
        multipart.addBodyPart(textBody);

        //enable if you want to send attachment too
        //filename = the name of file you want to upload get from Gallery or file etc
//        if (!(fileName.equals(""))) {
//            // Create new MimeBodyPart object and set DataHandler object to this object
//            MimeBodyPart attachmentBody = new MimeBodyPart();
//            String filename = fileName; // change accordingly
//            DataSource source = new FileDataSource(filename);
//            attachmentBody.setDataHandler(new DataHandler(source));
//            attachmentBody.setFileName(filename);
//            multipart.addBodyPart(attachmentBody);
//        }

        //Set the multipart object to the message object
        email.setContent(multipart);
        return email;
    }

    private Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
