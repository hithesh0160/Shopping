package com.instamart.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class SendTelegramMessage {

    public static void main(String[] args) throws Exception {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String chatId = System.getenv("TELEGRAM_CHAT_ID");

        if (botToken == null || chatId == null) {
            System.err.println("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID environment variables.");
            return;
        }

        // Surefire report directory
        Path reportsDir = Paths.get("shop", "test-output", "junitreports");
        if (!Files.exists(reportsDir)) {
            System.err.println("Surefire reports directory not found: " + reportsDir.toAbsolutePath());
            return;
        }

        StringBuilder extractedContent = new StringBuilder();

        try {
            // Read all .txt reports
            String allReports = Files.walk(reportsDir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> {
                        try {
                            return Files.readAllLines(p).stream().collect(Collectors.joining("\n"));
                        } catch (IOException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.joining("\n\n"));

            // Extract between markers
            String startMarker = "Product Details:";
            String endMarker = "[INFO] Tests run:";

            int startIndex = allReports.indexOf(startMarker);
            int endIndex = allReports.indexOf(endMarker, startIndex);

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String between = allReports.substring(startIndex + startMarker.length(), endIndex).trim();
                extractedContent.append(between);
            } else {
                extractedContent.append("No matching content found between the specified markers.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send message to Telegram
        sendTelegramMessage(botToken, chatId, extractedContent.toString());
    }

    private static void sendTelegramMessage(String botToken, String chatId, String text) throws Exception {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String urlParameters = "chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl + "?" + urlParameters).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        System.out.println("Telegram API Response Code: " + responseCode);
    }
}

