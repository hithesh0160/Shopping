package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendTelegramMessage {
    public static void main(String[] args) throws Exception {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String chatId = System.getenv("TELEGRAM_CHAT_ID");

        File file = new File("./shop/mvn_output.log");
        StringBuilder filteredLog = new StringBuilder();
        boolean capture = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Product Details:")) {
                    capture = true;
                }
                if (capture) {
                    if (line.contains("[INFO] Tests run:")) { // Stop before summary
                        break;
                    }
                    filteredLog.append(line).append("\n");
                }
            }
        }

        String message = filteredLog.toString().trim();
        if (!message.isEmpty()) {
            sendTelegramMessage(botToken, chatId, message);
        } else {
            System.out.println("No matching log section found.");
        }
    }

    private static void sendTelegramMessage(String botToken, String chatId, String text) throws Exception {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String urlParameters = "chat_id=" + chatId + "&text=" + java.net.URLEncoder.encode(text, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl + "?" + urlParameters).openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        System.out.println("Telegram API Response Code: " + responseCode);
    }
}
