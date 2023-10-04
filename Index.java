import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class OneDriveUploader {

    public static void uploadToOneDrive(String accessToken, String driveId, String folderId, String fileName, byte[] data) {
        try {
            String endpoint = "https://graph.microsoft.com/v1.0/me/drives/" + driveId + "/items/" + folderId + ":/" + fileName + ":/content";

            URL apiUrl = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            // Set the necessary headers for authentication and content type
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("File uploaded successfully.");
            } else {
                System.out.println("Error uploading file. Response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String username = "YOUR_USERNAME";  // OneDrive username
        String password = "YOUR_PASSWORD";  // OneDrive password
        String driveId = "YOUR_DRIVE_ID";  // The ID of the OneDrive drive
        String folderId = "YOUR_FOLDER_ID";  // The ID of the target folder
        String fileName = "sample.txt";
        byte[] data = "Hello, OneDrive!".getBytes();

        String basicAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String accessToken = "Bearer " + basicAuth;

        uploadToOneDrive(accessToken, driveId, folderId, fileName, data);
    }

    public void saveFileToWindowsShare(byte[] data, String fileName, String sharePath) {
        try {
            Path filePath = Paths.get(sharePath, fileName);
            Files.write(filePath, data);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }
}

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.apache.commons.io.FileUtils;

public class SendEmailWithAttachment {

    public static void main(String[] args) {
        final String username = "your_email@gmail.com"; // Your email address
        final String password = "your_password"; // Your email password
        String toAddress = "recipient@example.com"; // Recipient's email address
        String subject = "Subject of the email";
        String body = "Body of the email";
        String attachmentPath = "path_to_your_pdf_file.pdf"; // Replace with the actual path of your PDF file

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            // Create the attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("attachment.pdf");

            // Multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            // Set the content
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully.");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}



