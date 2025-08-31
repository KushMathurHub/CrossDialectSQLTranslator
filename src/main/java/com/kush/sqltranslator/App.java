package com.kush.sqltranslator;

import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParseException;

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
                    SqlParser parser = SqlParser.create(query);
                    parser.parseStmt();

                    String translated = translateQuery(query);
                    translatedQueries.add(translated);
                } catch (SqlParseException e) {
                    errors.add("Failed to parse query: " + query);
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
        translated = translated.replaceAll("(?i)NVL\\s*\\(", "IFNULL(");
        translated = translated.replaceAll("(?i)ROWNUM", "LIMIT 1");
        translated = translated.replaceAll("\\|\\|", ",");
        if (translated.contains(",")) {
            translated = "CONCAT(" + translated + ")";
        }
        translated = translated.replaceAll("(?i)FROM\\s+DUAL", "");

        return translated.trim();
    }
}






