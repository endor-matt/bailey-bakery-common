package com.baileybakery.common.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * PDF generation service for invoices, receipts, and order summaries.
 * Uses wkhtmltopdf for high-fidelity HTML-to-PDF conversion that preserves
 * the bakery's branded styling and layout.
 */
public class PdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerator.class);
    private static final String WKHTMLTOPDF = "/usr/local/bin/wkhtmltopdf";

    /**
     * Generates a PDF from an HTML string and saves it to the output path.
     * Used for generating order receipts, monthly invoices, and printable menus.
     *
     * @param htmlContent the HTML content to convert
     * @param outputPath the filesystem path to save the PDF
     * @return the output file path
     */
    public static String generate(String htmlContent, String outputPath) throws IOException, InterruptedException {
        // Write HTML to a temp file for wkhtmltopdf input
        Path tempHtml = Files.createTempFile("receipt-", ".html");
        Files.writeString(tempHtml, htmlContent);

        String command = WKHTMLTOPDF + " " + tempHtml.toAbsolutePath() + " " + outputPath;
        log.info("Generating PDF: {}", command);

        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        // Clean up temp file
        Files.deleteIfExists(tempHtml);

        if (exitCode != 0) {
            String error = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            throw new IOException("PDF generation failed (exit " + exitCode + "): " + error);
        }

        return outputPath;
    }

    /**
     * Generates a PDF from a URL (e.g., a rendered report page).
     * Used for exporting dashboard reports and analytics pages.
     *
     * @param sourceUrl the URL to render as PDF
     * @param outputPath the filesystem path to save the PDF
     * @return the output file path
     */
    public static String generateFromUrl(String sourceUrl, String outputPath) throws IOException, InterruptedException {
        String command = WKHTMLTOPDF + " " + sourceUrl + " " + outputPath;
        log.info("Generating PDF from URL: {}", command);

        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String error = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            throw new IOException("PDF generation failed (exit " + exitCode + "): " + error);
        }

        return outputPath;
    }
}
