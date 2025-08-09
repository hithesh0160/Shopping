package com.instamart.utils;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class SendTelegram {
    public static void main(String[] args) throws Exception {
        String logFilePath = args[0];
        String botToken = args[1];
        String chatId = args[2];

        String fullLog = new String(Files.readAllBytes(Paths.get(logFilePath)));
        List<String> chunks = splitMessage(fullLog, 4000);

        for (String chunk : chunks) {
            sendMessage(botToken, chatId, chunk);
        }
    }

    private static List<String> splitMessage(String message, int limit) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < message.length(); i += limit) {
            parts.add(message.substring(i, Math.min(i + limit, message.length())));
        }
        return parts;
    }

    private static void sendMessage(String token, String chatId, String text) {
        try {
            String urlString = "https://api.telegram.org/bot" + token + "/sendMessage";
            URI uri = URI.create(urlString);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String data = "chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            System.out.println("Sent: " + conn.getResponseCode());
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
