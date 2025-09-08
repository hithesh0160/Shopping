package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendTelegramMessage {

    private static final int TELEGRAM_LIMIT = 3900;
    private static final File PREVIOUS_LOG_FILE = new File("./shop/previous/product_details.log");
    private static final File CURRENT_LOG_FILE  = new File("./shop/product_details.log");
    private static final Logger logger = Logger.getLogger(SendTelegramMessage.class.getName());

    public static void main(String[] args) {
        try {
            String botToken = args.length > 0 ? args[0] : getenvOrFail("TELEGRAM_TOKEN");
            String chatId   = args.length > 1 ? args[1] : getenvOrFail("TELEGRAM_CHAT_ID");

            if (!CURRENT_LOG_FILE.exists()) {
                logger.severe("❌ Current log file not found: " + CURRENT_LOG_FILE.getAbsolutePath());
                return;
            }

            String currentMessage = readFile(CURRENT_LOG_FILE);

            if (currentMessage.isEmpty()) {
                logger.info("ℹ️ Product details section is empty. Nothing to send.");
                return;
            }

            if (isSameAsPrevious(currentMessage)) {
                logger.info("✅ Current logs match previous logs. Skipping Telegram message.");
            } else {
                logger.info("📩 Logs changed. Sending Telegram message...");
                sendToTelegramInChunks(botToken, chatId, currentMessage);
                logger.info("✅ Message sent.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred:", e);
        }
    }

    private static boolean isSameAsPrevious(String currentMessage) {
        if (!PREVIOUS_LOG_FILE.exists()) {
            logger.info("ℹ️ No previous log file found. Treating as first run.");
            return false;
        }

        try {
            String previousMessage = readFile(PREVIOUS_LOG_FILE);
            return currentMessage.trim().equals(previousMessage.trim());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read previous log file:", e);
            return false;
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String getenvOrFail(String key) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Missing env var: " + key);
        }
        return v;
    }

    private static void sendToTelegramInChunks(String botToken, String chatId, String text) throws IOException {
        int idx = 0;
        while (idx < text.length()) {
            int end = Math.min(idx + TELEGRAM_LIMIT, text.length());

            int lastNewline = text.lastIndexOf('\n', end);
            if (lastNewline > idx + 100) {
                end = lastNewline;
            }

            String part = text.substring(idx, end);
            sendTelegram(botToken, chatId, part);
            idx = end;
        }
    }

    private static void sendTelegram(String botToken, String chatId, String text) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8.name())
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.name());

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Failed to send message to Telegram: HTTP " + code);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                logger.info(line);
            }
        }
    }
}
