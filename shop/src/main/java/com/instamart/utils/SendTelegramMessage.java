package com.instamart.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SendTelegramMessage {

    public static void main(String[] args) throws Exception {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String chatId = System.getenv("TELEGRAM_CHAT_ID");

        if (botToken == null || chatId == null) {
            System.err.println("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID environment variables.");
            return;
        }

        Path reportsDir = Paths.get("target", "surefire-reports");
        if (!Files.exists(reportsDir)) {
            System.err.println("Surefire reports directory not found: " + reportsDir.toAbsolutePath());
            return;
        }

        StringBuilder reportSummary = new StringBuilder();

        // 1. Read TXT summaries (if any)
        try {
            List<String> txtReports = Files.walk(reportsDir)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> {
                        try {
                            return Files.readAllLines(p).stream().collect(Collectors.joining("\n"));
                        } catch (IOException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.toList());

            for (String report : txtReports) {
                for (String line : report.split("\n")) {
                    if (line.contains("Tests run:") || line.contains("<<< FAILURE!") || line.contains("<<< ERROR!")) {
                        reportSummary.append(line).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. Read XML summaries (more reliable)
        try {
            List<Path> xmlReports = Files.walk(reportsDir)
                    .filter(p -> p.toString().endsWith(".xml"))
                    .collect(Collectors.toList());

            for (Path xmlFile : xmlReports) {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(xmlFile.toFile());
                    doc.getDocumentElement().normalize();

                    Element root = doc.getDocumentElement();
                    if (root.getTagName().equals("testsuite")) {
                        String name = root.getAttribute("name");
                        String tests = root.getAttribute("tests");
                        String failures = root.getAttribute("failures");
                        String errors = root.getAttribute("errors");
                        String skipped = root.getAttribute("skipped");

                        reportSummary.append(String.format(
                                "%s -> Tests run: %s, Failures: %s, Errors: %s, Skipped: %s%n",
                                name, tests, failures, errors, skipped
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing XML file: " + xmlFile);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (reportSummary.length() == 0) {
            reportSummary.append("No test summary found in reports.");
        }

        sendTelegramMessage(botToken, chatId, reportSummary.toString().trim());
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
