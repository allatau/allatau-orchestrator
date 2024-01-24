package org.wscp.workflow_orchestrator.remote_clients;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

public class MicroserviceHTTPClient {

    public static String generate(String endpoint, String payload) throws IOException {
        // Make a POST request to the microservice endpoint
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write("data=example".getBytes());

        // Read the response as a JSON string
        InputStream responseStream = conn.getInputStream();
        Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
        String responseBody = scanner.hasNext() ? scanner.next() : "";

        // Parse the JSON to extract the "path" field
        System.out.println(responseBody);
        Gson Gson = new Gson();
        Map responseBodyParsed = Gson.fromJson(responseBody, Map.class);
        System.out.println(responseBodyParsed);
        String path = (String) responseBodyParsed.get("path");

        return path;
    }

    public static void main(String[] args) throws IOException {
          String s = generate("https://microservice-zero.vercel.app/generate", "{}");

          System.out.println(s);

//        InputStream inputStream = new URL("https://microservice-zero.vercel.app/media/cavity.zip").openStream();
//
//        System.out.println("downloaded:");
//
//        System.out.println(inputStream);
    }

}
