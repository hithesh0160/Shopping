package com.amazon.utils;

import java.net.http.*;
import java.net.URI;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

public class HtmlBodyExtractor {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the URL: ");
        String url = scanner.nextLine();
        scanner.close();

        String htmlBody = getHtmlBodyFromUrl(url);
        System.out.println("\n--- Response Content ---\n" + htmlBody);
    }

    public static String getHtmlBodyFromUrl(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept-Encoding", "gzip, deflate")
            .GET()
            .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String contentType = response.headers().firstValue("Content-Type").orElse("unknown");
        String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("none");
//        System.out.println("Content-Type: " + contentType);
//        System.out.println("Content-Encoding: " + contentEncoding);
        byte[] bodyBytes = response.body();
        // Print first 200 bytes as hex for diagnostics
//        System.out.print("First 200 bytes (hex): ");
        for (int i = 0; i < Math.min(200, bodyBytes.length); i++) {
//            System.out.printf("%02x ", bodyBytes[i]);
        }
//        System.out.println();
        // Try to extract charset from Content-Type
        String charset = "UTF-8";
        if (contentType.contains("charset=")) {
            charset = contentType.substring(contentType.indexOf("charset=") + 8).split("[;\"]")[0].trim();
        }
//        System.out.println("Detected charset: " + charset);
        // Handle gzip decompression if needed
        if (contentEncoding.equalsIgnoreCase("gzip")) {
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bodyBytes));
                 InputStreamReader isr = new InputStreamReader(gis, charset);
                 BufferedReader br = new BufferedReader(isr)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } else {
            try {
                return new String(bodyBytes, charset);
            } catch (Exception e) {
                System.out.println("Failed to decode with charset: " + charset + ". Showing as UTF-8.");
                return new String(bodyBytes, StandardCharsets.UTF_8);
            }
        }
    }
}