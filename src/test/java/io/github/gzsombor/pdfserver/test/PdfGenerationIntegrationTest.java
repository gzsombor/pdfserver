package io.github.gzsombor.pdfserver.test;

import io.github.gzsombor.pdfserver.test.config.TestConfig;
import io.github.gzsombor.pdfserver.test.helper.TestPdfResponseConverter;
import io.github.gzsombor.pdfserver.test.model.InvoiceData;
import io.github.gzsombor.pdfserver.test.model.InvoiceData.InvoiceItem;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PDF generation from templates and variables.
 * These tests verify that a PDF can be successfully created from:
 * - A Thymeleaf template
 * - A data model with various fields
 * - The Spring context configuration
 */
@SpringJUnitConfig(TestConfig.class)
public class PdfGenerationIntegrationTest {

    @Autowired
    private TestPdfResponseConverter pdfResponseConverter;

    @TempDir
    Path tempDir;

    @Test
    public void testGeneratePdfFromInvoiceTemplateAndVariables() throws Exception {
        // Given: Invoice data with various fields
        List<InvoiceItem> items = Arrays.asList(
            new InvoiceItem("Web Development Services", 40, 150.00),
            new InvoiceItem("UI/UX Design", 20, 120.00),
            new InvoiceItem("Cloud Hosting (Monthly)", 1, 500.00)
        );

        InvoiceData invoiceData = new InvoiceData(
            "INV-2025-001",
            LocalDate.of(2025, 11, 18),
            "Acme Corporation",
            "123 Main Street, New York, NY 10001",
            items
        );

        // When: Generate PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage outputMessage = createHttpOutputMessage(outputStream);
        
        pdfResponseConverter.writeToOutput(invoiceData, outputMessage);

        // Then: Verify PDF was created
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(1000); // PDF should have reasonable size

        // Verify PDF can be parsed
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            
            // Extract and verify text content
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertThat(text).contains("INVOICE");
            assertThat(text).contains("INV-2025-001");
            assertThat(text).contains("2025-11-18");
            assertThat(text).contains("Acme Corporation");
            assertThat(text).contains("123 Main Street, New York, NY 10001");
            assertThat(text).contains("Web Development Services");
            assertThat(text).contains("UI/UX Design");
            assertThat(text).contains("Cloud Hosting (Monthly)");
            assertThat(text).contains("8900.00"); // Total amount (40*150 + 20*120 + 1*500 = 8900)
        }

        // Save PDF to temp directory for manual inspection if needed
        File pdfFile = tempDir.resolve("test-invoice.pdf").toFile();
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            fos.write(pdfBytes);
        }
        assertThat(pdfFile).exists();
        System.out.println("PDF generated at: " + pdfFile.getAbsolutePath());
    }

    @Test
    public void testGeneratePdfWithDifferentData() throws Exception {
        // Given: Different invoice data to test template flexibility
        List<InvoiceItem> items = Arrays.asList(
            new InvoiceItem("Consulting Services", 10, 200.00),
            new InvoiceItem("Training Session", 5, 300.00)
        );

        InvoiceData invoiceData = new InvoiceData(
            "INV-2025-002",
            LocalDate.of(2025, 11, 19),
            "Tech Startup Inc.",
            "456 Innovation Drive, San Francisco, CA 94105",
            items
        );

        // When: Generate PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage outputMessage = createHttpOutputMessage(outputStream);
        
        pdfResponseConverter.writeToOutput(invoiceData, outputMessage);

        // Then: Verify PDF contains the correct data
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertThat(text).contains("INV-2025-002");
            assertThat(text).contains("Tech Startup Inc.");
            assertThat(text).contains("Consulting Services");
            assertThat(text).contains("Training Session");
            assertThat(text).contains("3500.00"); // Total: 2000 + 1500
        }
    }

    @Test
    public void testGeneratePdfWithSingleItem() throws Exception {
        // Given: Invoice with single item
        List<InvoiceItem> items = Arrays.asList(
            new InvoiceItem("Premium Support License", 1, 9999.99)
        );

        InvoiceData invoiceData = new InvoiceData(
            "INV-2025-003",
            LocalDate.now(),
            "Enterprise Client Ltd.",
            "789 Business Park, London, UK",
            items
        );

        // When: Generate PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage outputMessage = createHttpOutputMessage(outputStream);
        
        pdfResponseConverter.writeToOutput(invoiceData, outputMessage);

        // Then: Verify PDF is valid
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertThat(text).contains("Premium Support License");
            assertThat(text).contains("9999.99");
        }
    }

    @Test
    public void testOutputNameGeneration() {
        // Given: Invoice data
        InvoiceData invoiceData = new InvoiceData(
            "INV-2025-004",
            LocalDate.now(),
            "Test Customer",
            "Test Address",
            Arrays.asList(new InvoiceItem("Test Item", 1, 100.00))
        );

        // When/Then: Verify output name is correctly generated
        assertThat(invoiceData.getOutputName()).isEqualTo("invoice-INV-2025-004");
        assertThat(invoiceData.getTemplateName()).isEqualTo("invoice");
    }

    /**
     * Helper method to create a mock HttpOutputMessage for testing.
     */
    private HttpOutputMessage createHttpOutputMessage(ByteArrayOutputStream outputStream) {
        return new HttpOutputMessage() {
            private final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();

            @Override
            public OutputStream getBody() throws IOException {
                return outputStream;
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return headers;
            }
        };
    }
}
