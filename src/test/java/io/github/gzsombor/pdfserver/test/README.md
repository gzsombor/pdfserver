# PDF Generation Integration Tests

This directory contains comprehensive integration tests that prove PDFs can be created from templates and variables using the pdfserver library.

## Overview

The tests demonstrate the full PDF generation workflow:
1. Define data models implementing `PdfOutput` interface
2. Create Thymeleaf HTML templates
3. Configure Spring context with template engine
4. Generate PDFs from templates + variables
5. Validate PDF content using Apache PDFBox

## Test Structure

### Test Classes

#### `PdfGenerationIntegrationTest`
Tests basic PDF generation with single documents:
- `testGeneratePdfFromInvoiceTemplateAndVariables()` - Creates an invoice PDF with multiple line items
- `testGeneratePdfWithDifferentData()` - Verifies template reusability with different data
- `testGeneratePdfWithSingleItem()` - Tests edge case with single item
- `testOutputNameGeneration()` - Verifies output naming logic

#### `MergedPdfGenerationIntegrationTest`
Tests merged PDF generation from multiple sections:
- `testGenerateMergedPdfFromMultipleSections()` - Combines 4 report sections into one PDF
- `testGenerateMergedPdfWithTwoSections()` - Tests merging with minimal sections
- `testMergedPdfOutputName()` - Validates merged PDF naming

### Test Models

#### `InvoiceData` (`src/test/java/.../model/InvoiceData.java`)
- Implements `PdfOutput` and `PdfContextConfigurer`
- Contains invoice fields: number, date, customer info, line items
- Demonstrates complex data structures with nested objects

#### `ReportSection` (`src/test/java/.../model/ReportSection.java`)
- Simple model for report sections
- Used for testing merged PDFs
- Shows minimal implementation of PdfOutput

### Templates

#### `invoice.html` (`src/test/resources/templates/invoice.html`)
- Professional invoice template with Thymeleaf expressions
- Includes table iteration for line items
- Demonstrates number formatting
- Shows CSS styling in PDF

#### `report-section.html` (`src/test/resources/templates/report-section.html`)
- Simple report section template
- Used for merged PDF testing
- Shows basic Thymeleaf variable substitution

### Configuration

#### `TestConfig` (`src/test/java/.../config/TestConfig.java`)
- Spring configuration for tests
- Sets up `ClassLoaderTemplateResolver` for Thymeleaf
- Configures `SpringTemplateEngine`
- Provides `TestPdfResponseConverter` bean

#### `TestPdfResponseConverter` (`src/test/java/.../helper/TestPdfResponseConverter.java`)
- Test helper extending `PdfResponseConverter`
- Exposes protected `writeInternal()` method for testing

## Running the Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PdfGenerationIntegrationTest

# Run with verbose output
mvn test -X
```

## What the Tests Prove

### ✓ PDF Generation Works
All tests successfully generate valid PDF files that can be parsed by Apache PDFBox.

### ✓ Templates Are Processed
Thymeleaf templates are correctly processed with variable substitution, iteration, and formatting.

### ✓ Data Binding Works
Complex data structures (objects with nested lists) are properly bound to template variables.

### ✓ Content is Correct
PDFs contain the expected text content extracted from templates and variables.

### ✓ Merged PDFs Work
Multiple template sections can be combined into a single PDF document.

### ✓ Configuration is Valid
Spring context properly wires together all components needed for PDF generation.

## Test Output

Tests generate PDFs in temporary directories and print their locations:
```
PDF generated at: /tmp/junit-xxx/test-invoice.pdf
Merged PDF generated at: /tmp/junit-xxx/merged-report.pdf
```

You can manually inspect these files during test runs to verify visual output.

## Dependencies

The tests use:
- **JUnit 5** (5.11.4) - Test framework
- **Spring Test** (6.2.13) - Spring testing support
- **AssertJ** (3.27.3) - Fluent assertions
- **Apache PDFBox** (3.0.3) - PDF parsing and validation
- **Thymeleaf Spring6** (3.1.3) - Template engine
- **Flying Saucer PDF** (9.4.0) - HTML to PDF rendering

## Example: Creating Your Own Test

```java
@Test
public void testMyPdfGeneration() throws Exception {
    // 1. Create your data model
    MyData data = new MyData("value1", "value2");
    
    // 2. Generate PDF
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HttpOutputMessage outputMessage = createHttpOutputMessage(outputStream);
    pdfResponseConverter.writeToOutput(data, outputMessage);
    
    // 3. Validate PDF
    byte[] pdfBytes = outputStream.toByteArray();
    assertThat(pdfBytes).isNotEmpty();
    
    try (PDDocument document = Loader.loadPDF(pdfBytes)) {
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        assertThat(text).contains("value1");
    }
}
```

## Notes

- Tests use `@TempDir` to automatically clean up generated PDFs
- SLF4J warnings about missing providers are expected and harmless
- PDF text extraction may include line breaks and formatting differences
- Number formatting in PDFs uses default locale settings
