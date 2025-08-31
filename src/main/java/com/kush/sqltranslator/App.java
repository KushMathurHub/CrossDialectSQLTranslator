package com.kush.sqltranslator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) {
        String inputFile = "queries.txt";
        String outputFile = "translated_queries.txt";
        String errorFile = "error_log.txt";

        try {
            List<String> queries = readQueries(inputFile);
            List<String> translatedQueries = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String query : queries) {
                try {
                    String translated = translateQuery(query);
                    translatedQueries.add(translated);
                } catch (Exception e) {
                    errors.add("Failed to translate query: " + query);
                }
            }

            Files.write(Paths.get(outputFile), translatedQueries, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(Paths.get(errorFile), errors, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Translation complete. Check output files.");
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }

    private static List<String> readQueries(String filePath) throws IOException {
        List<String> queries = new ArrayList<>();
        StringBuilder currentQuery = new StringBuilder();

        for (String line : Files.readAllLines(Paths.get(filePath))) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("--")) continue;

            currentQuery.append(line).append(" ");

            if (line.endsWith(";")) {
                queries.add(currentQuery.toString().trim());
                currentQuery.setLength(0);
            }
        }
        if (currentQuery.length() > 0) {
            queries.add(currentQuery.toString().trim());
        }
        return queries;
    }

    private static String translateQuery(String query) {
        String translated = query;

        translated = translated.replaceAll("(?i)SYSDATE", "CURRENT_DATE()");
        translated = translated.replaceAll("(?i)SYSTIMESTAMP", "CURRENT_TIMESTAMP()");
        translated = translated.replaceAll("(?i)NVL\\s*\\(", "IFNULL(");
        translated = translated.replaceAll("(?i)TRUNC\\s*\\(([^,]+),\\s*0\\)", "FLOOR($1)");
        translated = translated.replaceAll("(?i)ROUND\\s*\\(([^,]+),\\s*(\\d+)\\)", "ROUND($1, $2)");
        translated = translated.replaceAll("(?i)FROM\\s+DUAL", "");
        translated = translated.replaceAll("(\\w+)\\s*\\|\\|\\s*(\\w+)", "CONCAT($1, $2)");
        translated = translated.replaceAll("(?i)TO_CHAR\\s*\\(([^,]+),\\s*'([^']+)'\\)", "DATE_FORMAT($1, '$2')");
        translated = translated.replaceAll("(?i)TO_DATE\\s*\\(([^,]+),\\s*'([^']+)'\\)", "STR_TO_DATE($1, '$2')");
        translated = translated.replaceAll("(?i)WHERE\\s+ROWNUM\\s*<=\\s*(\\d+)", "LIMIT $1");

        if (!translated.endsWith(";")) {
            translated += ";";
        }

        return translated.trim();
    }
}







