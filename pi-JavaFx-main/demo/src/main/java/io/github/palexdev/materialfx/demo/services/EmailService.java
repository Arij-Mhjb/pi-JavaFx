package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Order;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.DeviceRgb;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;

public class EmailService {
    // Updated API key and sender configuration
    private static final String RESEND_API_KEY = "re_Mdk25Aec_M8Cg16bGSwKpdptwFQcF9117"; // Replace with your actual API key
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String SENDER_EMAIL = "onboarding@resend.dev"; // Using Resend's verified domain

    public void sendPaymentConfirmation(String userEmail, List<Order> orders, double totalAmount) {
        try {
            System.out.println("\n==== Generating PDF Invoice ====");
            
            // Generate PDF
            byte[] pdfBytes = generatePDF(orders, totalAmount);
            String base64PDF = Base64.getEncoder().encodeToString(pdfBytes);
            
            System.out.println("PDF generated successfully");
            
            // Create email content
            String emailContent = String.format(
                "Dear Customer,\n\n" +
                "Thank you for your purchase! Your payment has been successfully processed.\n\n" +
                "Please find attached your invoice with the complete order details.\n\n" +
                "Best regards,\nSportify Team"
            );

            // Create JSON payload with PDF attachment
            String jsonPayload = String.format(
                "{" +
                "\"from\":\"%s\"," +
                "\"to\":[\"%s\"]," +
                "\"subject\":\"Payment Confirmation - Sportify\"," +
                "\"text\":\"%s\"," +
                "\"attachments\":[{" +
                    "\"filename\":\"invoice.pdf\"," +
                    "\"content\":\"%s\"" +
                "}]" +
                "}",
                SENDER_EMAIL,
                userEmail,
                emailContent.replace("\n", "\\n").replace("\"", "\\\""),
                base64PDF
            );

            System.out.println("Preparing to send email with PDF attachment...");
            System.out.println("To: " + userEmail);
            
            // Set up HTTP connection
            URL url = new URL(RESEND_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + RESEND_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get response
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);

            // Read the response body
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), 
                    StandardCharsets.UTF_8))) {
                String responseBody = br.lines().collect(Collectors.joining("\n"));
                System.out.println("Response body: " + responseBody);
            }

            if (responseCode != 200) {
                throw new RuntimeException("Failed to send email. Response code: " + responseCode);
            }

            System.out.println("Payment confirmation email with PDF sent successfully to " + userEmail);
            System.out.println("==========================================");

        } catch (Exception e) {
            System.err.println("\n==== Error Sending Email ====");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("==========================");
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private byte[] generatePDF(List<Order> orders, double totalAmount) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add header
        Paragraph header = new Paragraph("Sportify - Invoice")
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        // Add date
        document.add(new Paragraph()
            .setFontSize(12)
            .add("Date: " + java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        document.add(new Paragraph("\n"));

        // Add order details
        Paragraph orderTitle = new Paragraph("Order Details:")
            .setFontSize(14)
            .setBold();
        document.add(orderTitle);
        document.add(new Paragraph("\n"));

        for (Order order : orders) {
            document.add(new Paragraph()
                .setFontSize(12)
                .add("Product: " + order.getProductName()));
            document.add(new Paragraph()
                .setFontSize(12)
                .add("Quantity: " + order.getQuantity()));
            document.add(new Paragraph()
                .setFontSize(12)
                .add(String.format("Price: $%.2f", order.getTotalPrice())));
            document.add(new Paragraph()
                .setFontSize(12)
                .add("Order ID: " + order.getOrderId()));
            document.add(new Paragraph()
                .setFontSize(12)
                .add("Date: " + order.getDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            document.add(new Paragraph("\n"));
        }

        // Add total
        document.add(new Paragraph()
            .setFontSize(14)
            .setBold()
            .add(String.format("Total Amount: $%.2f", totalAmount)));

        // Add footer
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Thank you for shopping with Sportify!")
            .setFontSize(10)
            .setItalic()
            .setFontColor(new DeviceRgb(128, 128, 128))); // Gray color

        document.close();
        return baos.toByteArray();
    }
} 