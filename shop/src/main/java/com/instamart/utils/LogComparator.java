package com.instamart.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class LogComparator {

    public String compareLogs(File previous, File current) throws IOException {
        if (!previous.exists()) {
            System.out.println("No previous log found. Treating current log as new.");
            return new String(Files.readAllBytes(current.toPath()));
        }

        List<String> prevLines = Files.readAllLines(previous.toPath());
        List<String> currLines = Files.readAllLines(current.toPath());

        StringBuilder diff = new StringBuilder();

        for (String line : currLines) {
            if (!prevLines.contains(line)) {
                diff.append(line).append("\n");
            }
        }

        return diff.toString().trim();
    }
}
