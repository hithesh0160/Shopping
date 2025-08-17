package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendTelegramMessage {
    public static void main(String[] args) {
        try {
            String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
            String chatId   = System.getenv("TELEGRAM_CHAT_ID");

            if (botToken == null || chatId == null) {
                System.err.println("❌ Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID environment variables.");
                return;
            }

            File file = new File("./shop/mvn_output.log");
            if (!file.exists()) {
                System.err.println("❌ Log file not found: " + file.getAbsolutePath());
                return;
            }

            String message = extractLogSection(file);
            if (!message.isEmpty()) {
                sendTelegramMessage(botToken, chatId, message);
            } else {
                System.out.println("ℹ️ No matching log section found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Extracts the log section between "Product Details:" and before "[INFO] Tests run:" */
    private static String extractLogSection(File file) throws IOException {
        StringBuilder filteredLog = new StringBuilder();
        boolean capture = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!capture && line.contains("Product Details:")) {
                    capture = true;
                }

                if (capture) {
                    if (line.contains("[INFO] Tests run:")) {
                        // ✅ stop before appending summary
                        break;
                    }
                    filteredLog.append(line).append("\n");
                }
            }
        }
        return filteredLog.toString().trim();
    }

    /** Sends the given text to Telegram */
    private static void sendTelegramMessage(String botToken, String chatId, String text) throws Exception {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String urlParameters = "chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl + "?" + urlParameters).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        System.out.println("✅ Telegram API Response Code: " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
