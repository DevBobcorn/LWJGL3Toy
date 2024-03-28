package io.devbobcorn.toy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;

public class Utils {

    private Utils() {
        // Utility class
    }

    public static String readFile(String filePath) {
        String str;
        try {
            var classloader = Thread.currentThread().getContextClassLoader();
            var inputStream = classloader.getResourceAsStream(filePath);

            var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            var sb = new StringBuilder();
            for (var line : new BufferedReader(streamReader).lines().toList()) {
                sb.append(line).append('\n');
            }
            str = sb.toString();
        } catch (Exception excp) {
            excp.printStackTrace();
            throw new RuntimeException("Error reading file [" + filePath + "]: " + excp);
        }
        return str;
    }
}