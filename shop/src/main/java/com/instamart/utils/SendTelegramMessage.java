package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendTelegramMessage {

    private static final int TELEGRAM_LIMIT = 3900; // keep under 4096 to be safe

    public static void main(String[] args) {
        try {
            String botToken = getenvOrFail("TELEGRAM_BOT_TOKEN");
            String chatId   = getenvOrFail("TELEGRAM_CHAT_ID");

            File file = new File("./shop/mvn_output.log");
            if (!file.exists()) {
                System.err.println("❌ Log file not found: " + file.getAbsolutePath());
                return;
            }

            String message = extractSection(file,
                    "Product Details:",            // start marker
                    "^\\s*\\[INFO\\]\\s+Tests run:" // end marker (regex), color-safe after stripping
            );

            if (message.isEmpty()) {
                System.out.println("ℹ️ No matching 'Product Details' section found.");
                return;
            }

            sendToTelegramInChunks(botToken, chatId, message);
        } catch (Exception e) {
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

    /** Reads file, strips ANSI, returns text between start literal and first line matching endRegex. */
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
                        // stop BEFORE the summary line
                        break;
                    }
                    out.append(line).append('\n');
                }
            }
        }
        return out.toString().trim();
    }

    /** Remove ANSI escape sequences (colors, cursor moves, etc.). */
    private static String stripAnsi(String s) {
        // General ANSI escape sequence pattern
        return s.replaceAll("\u001B\\[[;?0-9]*[ -/]*[@-~]", "");
    }

    private static void sendToTelegramInChunks(String botToken, String chatId, String text) throws IOException {
        int idx = 0;
        while (idx < text.length()) {
            int end = Math.min(idx + TELEGRAM_LIMIT, text.length());

            // try to break on a newline if possible
            int lastNewline = text.lastIndexOf('\n', end);
            if (lastNewline > idx && (end - lastNewline) < 300) {
                end = lastNewline;
            }

            String part = text.substring(idx, end);
            sendTelegram(botToken, chatId, part);
            idx = end;
        }
    }

    /** Simple POST without parse_mode to avoid Markdown/HTML parsing errors. */
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
