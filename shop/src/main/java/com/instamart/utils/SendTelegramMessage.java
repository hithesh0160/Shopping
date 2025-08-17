package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendTelegramMessage {

    private static final int TELEGRAM_LIMIT = 3900; // keep under 4096 to be safe
    private static final File PREVIOUS_LOG_FILE = new File("./shop/previous.log");
    private static final File CURRENT_LOG_FILE  = new File("./shop/mvn_output.log");

    public static void main(String[] args) {
        try {
            String botToken = getenvOrFail("TELEGRAM_BOT_TOKEN");
            String chatId   = getenvOrFail("TELEGRAM_CHAT_ID");

            if (!CURRENT_LOG_FILE.exists()) {
                System.err.println("❌ Log file not found: " + CURRENT_LOG_FILE.getAbsolutePath());
                return;
            }

            String message = extractSection(CURRENT_LOG_FILE,
                    "Product Details:",
                    "^\\s*\\[INFO\\]\\s+Tests run:"
            );

            if (message.isEmpty()) {
                System.out.println("ℹ️ No matching 'Product Details' section found.");
                return;
            }

            // Compare with previous run
            if (isSameAsPrevious(message)) {
                System.out.println("✅ No change since last run. Skipping Telegram send.");
            } else {
                System.out.println("📩 Change detected. Sending to Telegram...");
                sendToTelegramInChunks(botToken, chatId, message);
                saveAsPrevious(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isSameAsPrevious(String currentMessage) {
        if (!PREVIOUS_LOG_FILE.exists()) {
            System.out.println("ℹ️ No previous log found. Will treat as new run.");
            return false;
        }
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(PREVIOUS_LOG_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return currentMessage.trim().equals(sb.toString().trim());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void saveAsPrevious(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PREVIOUS_LOG_FILE))) {
            bw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
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
            if (lastNewline > idx && (end - lastNewline) < 300) {
                end = lastNewline;
            }

            String part = text.substring(idx, end);
            sendTelegram(botToken, chatId, part);
            idx = end;
        }
    }

    private static void sendTelegram(String botToken, String chatId, String text) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String body = "chat_id=" + URLEncoder.encode(chatId, "UTF-8")
                + "&text="   + URLEncoder.encode(text, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        System.out.println("Telegram HTTP " + code);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
