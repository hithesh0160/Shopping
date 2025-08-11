package com.instamart.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendTelegramMessage {
    public static void main(String[] args) throws Exception {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String chatId = System.getenv("TELEGRAM_CHAT_ID");

        File file = new File("test_output.log");
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


// public class SendTelegramMessage {

//     public static void main(String[] args) throws Exception {
//         String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
//         String chatId = System.getenv("TELEGRAM_CHAT_ID");

//         if (botToken == null || chatId == null) {
//             System.err.println("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID environment variables.");
//             return;
//         }

//         // Read all Surefire report files
//         Path reportsDir = Paths.get("target", "surefire-reports");
//         if (!Files.exists(reportsDir)) {
//             System.err.println("Surefire reports directory not found: " + reportsDir.toAbsolutePath());
//             return;
//         }

//         StringBuilder reportSummary = new StringBuilder();
//         try {
//             // Combine all .txt reports into one string
//             String allReports = Files.walk(reportsDir)
//                     .filter(p -> p.toString().endsWith(".txt"))
//                     .map(p -> {
//                         try {
//                             return Files.readAllLines(p).stream().collect(Collectors.joining("\n"));
//                         } catch (IOException e) {
//                             return "";
//                         }
//                     })
//                     .collect(Collectors.joining("\n\n"));

//             // Extract summary lines
//             for (String line : allReports.split("\n")) {
//                 if (line.contains("Tests run:") || line.contains("<<< FAILURE!") || line.contains("<<< ERROR!")) {
//                     reportSummary.append(line).append("\n");
//                 }
//             }

//             if (reportSummary.length() == 0) {
//                 reportSummary.append("No test summary found in reports.");
//             }

//         } catch (IOException e) {
//             e.printStackTrace();
//         }

//         // Send message
//         sendTelegramMessage(botToken, chatId, reportSummary.toString().trim());
//     }
// }

