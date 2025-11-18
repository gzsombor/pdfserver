package io.github.gzsombor.pdfserver.test;

import io.github.gzsombor.pdfserver.api.BasicMergedPdfOutput;
import io.github.gzsombor.pdfserver.api.MergedPdfOutput;
import io.github.gzsombor.pdfserver.api.PdfOutput;
import io.github.gzsombor.pdfserver.test.config.TestConfig;
import io.github.gzsombor.pdfserver.test.helper.TestPdfResponseConverter;
import io.github.gzsombor.pdfserver.test.model.ReportSection;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for merged PDF generation.
 * Tests that multiple sections can be merged into a single PDF document.
 */
@SpringJUnitConfig(TestConfig.class)
public class MergedPdfGenerationIntegrationTest {

    @Autowired
    private TestPdfResponseConverter pdfResponseConverter;

    @TempDir
    Path tempDir;

    @Test
    public void testGenerateMergedPdfFromMultipleSections() throws Exception {
        // Given: Multiple report sections
        List<PdfOutput> sections = Arrays.asList(
            new ReportSection("1", "Introduction", 
                "This is the introduction section of our comprehensive report. " +
                "It provides an overview of the topics covered in the subsequent sections."),
            new ReportSection("2", "Methodology", 
                "This section describes the methodology used in our research. " +
                "We employed a mixed-methods approach combining quantitative and qualitative analysis."),
            new ReportSection("3", "Results", 
                "The results section presents our key findings. " +
                "We observed significant improvements across all measured metrics."),
            new ReportSection("4", "Conclusion", 
                "In conclusion, this study demonstrates the effectiveness of our approach. " +
                "Future work should focus on scaling these findings to larger populations.")
        );

        MergedPdfOutput mergedReport = new BasicMergedPdfOutput("complete-report", sections);

        // When: Generate merged PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage outputMessage = createHttpOutputMessage(outputStream);
        
        pdfResponseConverter.writeToOutput(mergedReport, outputMessage);

        // Then: Verify merged PDF was created
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(1000);

        // Verify PDF contains all sections
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify all sections are present
            assertThat(text).contains("Section 1");
            assertThat(text).contains("Introduction");
            assertThat(text).contains("comprehensive report");
            
            assertThat(text).contains("Section 2");
            assertThat(text).contains("Methodology");
            // Text may have line breaks in the PDF, so check for "mixed" and "methods" separately
            assertThat(text).contains("mixed");
            assertThat(text).contains("methods");
            
            assertThat(text).contains("Section 3");
            assertThat(text).contains("Results");
            assertThat(text).contains("key findings");
            
            assertThat(text).contains("Section 4");
            assertThat(text).contains("Conclusion");
            assertThat(text).contains("effectiveness of our approach");
        }

        // Save PDF for inspection
        File pdfFile = tempDir.resolve("merged-report.pdf").toFile();
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            fos.write(pdfBytes);
        }
        assertThat(pdfFile).exists();
        System.out.println("Merged PDF generated at: " + pdfFile.getAbsolutePath());
    }

    @Test
    public void testGenerateMergedPdfWithTwoSections() throws Exception {
        // Given: Two sections
        List<PdfOutput> sections = Arrays.asList(
            new ReportSection("1", "Executive Summary", 
                "This executive summary highlights the key points of our analysis."),
            new ReportSection("2", "Recommendations", 
                "Based on our findings, we recommend the following actions.")
        );

        MergedPdfOutput mergedReport = new BasicMergedPdfOutput("summary-report", sections);

        // When: Generate merged PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage outputMessage = createHttpOutputMessage(outputStream);
        
        pdfResponseConverter.writeToOutput(mergedReport, outputMessage);

        // Then: Verify merged PDF is valid
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertThat(text).contains("Executive Summary");
            assertThat(text).contains("Recommendations");
        }
    }

    @Test
    public void testMergedPdfOutputName() {
        // Given: Merged PDF output
        List<PdfOutput> sections = Arrays.asList(
            new ReportSection("1", "Test", "Content")
        );
        MergedPdfOutput mergedReport = new BasicMergedPdfOutput("my-report", sections);

        // When/Then: Verify output name
        assertThat(mergedReport.getOutputName()).isEqualTo("my-report");
        assertThat(mergedReport.getIndividualPdfs()).hasSize(1);
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
