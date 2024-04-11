import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

@RestController
public class MyRestController {

    @PostMapping("/processRequest")
    public String processRequest(@RequestBody String requestJson) {
        // Parse incoming JSON
        // Example: JSONObject json = new JSONObject(requestJson);

        // Update JSON with request variables
        // Example: json.put("key", value);

        // Call another API
        String apiUrl = "https://example.com/api";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Content-Type", "application/json");
        try {
            StringEntity params = new StringEntity(requestJson);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            // Handle response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Process successful response
                return "Success";
            } else {
                // Handle error response
                return "Error";
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
            return "Exception occurred";
        }
    }
}


------------------


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.IOException;

public class JsonHandler {

    // Function to query JSON from a file
    public static JsonObject queryJsonFromFile(String filePath) throws IOException {
        // Read JSON from file
        FileReader reader = new FileReader(filePath);
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(reader);
        reader.close();
        return jsonObject;
    }

    // Function to parse JSON
    public static void parseJson(JsonObject jsonObject) {
        // Parse JSON data as needed
        String value = jsonObject.get("key").getAsString();
        System.out.println("Value of key: " + value);
    }

    public static void main(String[] args) {
        try {
            // Query JSON from file
            JsonObject json = queryJsonFromFile("example.json");

            // Parse JSON
            parseJson(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
