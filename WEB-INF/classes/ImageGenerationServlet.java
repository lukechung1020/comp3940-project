import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;


public class ImageGenerationServlet extends HttpServlet {
    private static String generate_image = "https://api.picogen.io/v1/job/generate";
    private static String get_image = "https://api.picogen.io/v1/job/get/";
    private static final String SECRETS_PROPERTIES_PATH = "/WEB-INF/secrets.properties";
    private static String api_key;

    public static String executePost(String targetURL, String jsonBody) {
        System.out.println("Executing POST to " + targetURL);
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json"); // Set JSON content type
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("API-Token", api_key);

            connection.setRequestProperty("Content-Length", Integer.toString(jsonBody.getBytes().length));
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Send request
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(jsonBody); // Write JSON body
                wr.flush();
            }

            // Get response
            int status = connection.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? connection.getInputStream() : connection.getErrorStream();

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String executeGet(String targetURL) {
        System.out.println("Executing GET to " + targetURL);
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("API-Token", api_key);
            connection.setRequestProperty("Accept", "application/json");

            // Get response
            int status = connection.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? connection.getInputStream() : connection.getErrorStream();

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Inside doGet");
        response.getWriter().print("Working getter");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Inside doPost");

        Properties secrets = new Properties();
        String filePath = getServletContext().getRealPath("/WEB-INF/secrets.properties");
        try(InputStream input = new FileInputStream(filePath)) {
            secrets.load(input);

            api_key = secrets.getProperty("api.key");
        } catch(IOException ioe) {
            throw new IOException("Failed to load from secrets.properties");
        }

        try {
            String question = request.getParameter("question");
            System.out.println("Request Question: " + question);
    
            // Use the question to generate a related image
            JSONObject JSONBody = new JSONObject();
            JSONBody.put("prompt", question);
            JSONBody.put("ratio", "16:9");
    
            String imageResponse = executePost(generate_image, JSONBody.toString());
            System.out.println("Generated image response: " + imageResponse);
            JSONArray imageResponseJSON = new JSONArray(imageResponse);
            JSONObject imageInfoJSON = imageResponseJSON.getJSONObject(1);
            String imageID = imageInfoJSON.getString("id");
    
            // Use id to get the generated image url
            long startTime = System.currentTimeMillis();
            long timeout = 30000; // 30 seconds timeout

            while (System.currentTimeMillis() - startTime < timeout) { 
                String generatedImageResponse = executeGet(get_image + imageID);
                System.out.println("Generated Image URL Response: " + generatedImageResponse);
                JSONArray generatedImageJSON = new JSONArray(generatedImageResponse);
                JSONObject generatedImageInfoJSON = generatedImageJSON.getJSONObject(1);

                String status = generatedImageInfoJSON.getString("status");

                if(status.equals("completed")) {
                    String imageURL = generatedImageInfoJSON.getString("result");
                    System.out.println("Image URL: " + imageURL);
                    JSONObject imageURLJSON = new JSONObject();
                    imageURLJSON.put("image_url", imageURL);
                    response.setContentType("application/json");
                    response.getWriter().print(imageURLJSON); 

                    return;
                } 
                Thread.sleep(1000);
            }
            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, "Image generation timed out");
  
        } catch (Exception e) {
            response.getWriter().print(e);
        }

    }
}