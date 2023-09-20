import java.io.*;
import java.net.*;

public class SharePointUploader {

    public static void uploadToSharePoint(String url, String username, String password, String fileName, byte[] data) {
        try {
            String endpoint = url + "/_api/web/getfolderbyserverrelativeurl('/Shared Documents')/files/add(url='" + fileName + "',overwrite=true)";

            URL apiUrl = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            // Set the necessary headers for authentication and content type
            connection.setRequestProperty("Authorization", "Basic " + encodeBase64(username + ":" + password));
            connection.setRequestProperty("Content-Type", "application/json;odata=verbose");
            connection.setRequestMethod("POST");
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

    private static String encodeBase64(String s) {
        return java.util.Base64.getEncoder().encodeToString(s.getBytes());
    }

    public static void main(String[] args) {
        String url = "https://your-sharepoint-site.com";
        String username = "your-username";
        String password = "your-password";
        String fileName = "sample.txt";
        byte[] data = "Hello, SharePoint!".getBytes();

        uploadToSharePoint(url, username, password, fileName, data);
    }
}
