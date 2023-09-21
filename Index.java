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
