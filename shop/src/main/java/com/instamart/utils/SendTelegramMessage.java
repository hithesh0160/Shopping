package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendTelegramMessage {

    private static final int TELEGRAM_LIMIT = 3900; // keep under 4096 to be safe
    private static final File PREVIOUS_LOG_FILE = new File("./shop/previous.log");
    private static final File CURRENT_LOG_FILE  = new File("./shop/mvn_output.log");
    private static final Logger logger = Logger.getLogger(SendTelegramMessage.class.getName());

    public static void main(String[] args) {
        try {
            String botToken = args.length > 0 ? args[0] : getenvOrFail("TELEGRAM_TOKEN");
            String chatId   = args.length > 1 ? args[1] : getenvOrFail("TELEGRAM_CHAT_ID");

            if (!CURRENT_LOG_FILE.exists()) {
                logger.severe("❌ Log file not found: " + CURRENT_LOG_FILE.getAbsolutePath());
                return;
            }

            String message = extractSection(CURRENT_LOG_FILE,
                    "Product Details:",
                    "^\\[INFO\\] Tests run: .* - in .*");

            if (message.isEmpty()) {
                logger.info("ℹ️ No matching 'Product Details' section found.");
                return;
            }

            if (isSameAsPrevious(message)) {
                logger.info("✅ No change since last run. Skipping Telegram send.");
            } else {
                logger.info("📩 Change detected. Sending to Telegram...");
                sendToTelegramInChunks(botToken, chatId, message);
                saveAsPrevious(message);
                logger.info("✅ Message successfully sent and saved.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred:", e);
        }
    }

    private static boolean isSameAsPrevious(String currentMessage) {
        if (!PREVIOUS_LOG_FILE.exists()) {
            logger.info("ℹ️ No previous log found. Will treat as new run.");
            return false;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(PREVIOUS_LOG_FILE))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return currentMessage.trim().equals(sb.toString().trim());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read previous log file:", e);
            return false;
        }
    }

    private static void saveAsPrevious(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PREVIOUS_LOG_FILE))) {
            bw.write(message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save previous log file:", e);
        }
    }

    private static String getenvOrFail(String key) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Missing env var: " + key);
        }
        return v;
    }

    private static String extractSection(File file, String startLiteral, String endRegex) throws IOException {
        StringBuilder out = new StringBuilder();
        boolean capture = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String raw;
            while ((raw = br.readLine()) != null) {
                String line = stripAnsi(raw);

                if (!capture && line.contains(startLiteral)) {
                    capture = true;
                    continue; // Skip the line containing startLiteral
                }

                if (capture) {
                    if (line.matches(endRegex)) {
                        break;
                    }
                    out.append(line).append('\n');
                }
            }
        }
        return out.toString().trim();
    }

    private static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;?0-9]*[ -/]*[@-~]", "");
    }

    private static void sendToTelegramInChunks(String botToken, String chatId, String text) throws IOException {
        int idx = 0;
        while (idx < text.length()) {
            int end = Math.min(idx + TELEGRAM_LIMIT, text.length());

            int lastNewline = text.lastIndexOf('\n', end);
            if (lastNewline > idx + 100) { // Avoid very small chunks
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

