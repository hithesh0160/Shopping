import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

public class SendTelegramMessage {

    public static void main(String[] args) throws Exception {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String chatId = System.getenv("TELEGRAM_CHAT_ID");

        if (botToken == null || chatId == null) {
            System.err.println("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID environment variables.");
            return;
        }

        StringBuilder reportSummary = new StringBuilder();

        // Path to TestNG testng-results.xml
        Path testngResults = Paths.get("shop", "test-output", "testng-results.xml");
        if (Files.exists(testngResults)) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(testngResults.toFile());
                doc.getDocumentElement().normalize();

                Element root = doc.getDocumentElement();
                String total = root.getAttribute("total");
                String passed = root.getAttribute("passed");
                String failed = root.getAttribute("failed");
                String skipped = root.getAttribute("skipped");

                reportSummary.append(String.format(
                        "TestNG Results -> Total: %s, Passed: %s, Failed: %s, Skipped: %s%n",
                        total, passed, failed, skipped
                ));
            } catch (Exception e) {
                System.err.println("Error parsing testng-results.xml");
                e.printStackTrace();
            }
        }

        // Parse JUnit-style XML reports from TestNG (shop/test-output/junitreports)
        Path junitReportsDir = Paths.get("shop", "test-output", "junitreports");
        if (Files.exists(junitReportsDir)) {
            try {
                List<Path> xmlReports = Files.walk(junitReportsDir)
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
                        System.err.println("Error parsing JUnit XML: " + xmlFile);
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
