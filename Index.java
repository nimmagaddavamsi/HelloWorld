import javax.net.ssl.SSLContext;
import java.net.HttpURLConnection;
import java.net.URL;

public class TlsCheck {
    public static void main(String[] args) {
        try {
            // Force TLS 1.2
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, null);
            SSLContext.setDefault(context);

            // Make a connection to the API
            URL url = new URL("https://<api-url>");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Check the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            System.out.println("Connection successful with TLS 1.2");
        } catch (Exception e) {
            System.out.println("TLS 1.2 connection failed: " + e.getMessage());
        }
    }
}
