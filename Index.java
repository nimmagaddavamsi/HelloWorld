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
-----------

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;

public class APIHeaderReader {

    public static void main(String[] args) {
        String filePath = "api_headers.json";

        try {
            JSONObject headers = readHeadersFromFile(filePath);
            System.out.println("Headers read from file: " + headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readHeadersFromFile(String filePath) throws Exception {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            return (JSONObject) jsonObject.get("headers");
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error reading headers from file: " + e.getMessage());
        }
    }
}
--------------

    import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class APIHeaderUpdater {

    public static void main(String[] args) {
        String filePath = "api_headers.json";

        try {
            JSONObject headers = readHeadersFromFile(filePath);
            System.out.println("Current Headers: " + headers);

            // Update headers
            headers.put("Authorization", "Bearer new_api_token_here");
            headers.put("Custom-Header", "custom_value");

            // Write updated headers back to file
            writeHeadersToFile(headers, filePath);

            System.out.println("Updated Headers: " + headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readHeadersFromFile(String filePath) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            return (JSONObject) jsonObject.get("headers");
        }
    }

    public static void writeHeadersToFile(JSONObject headers, String filePath) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("headers", headers);

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonObject.toJSONString());
        }
    }
}

    
