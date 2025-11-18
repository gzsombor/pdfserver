package io.github.gzsombor.pdfserver.test.helper;

import io.github.gzsombor.pdfserver.api.PdfOutput;
import io.github.gzsombor.pdfserver.impl.PdfResponseConverter;
import org.springframework.http.HttpOutputMessage;

import java.io.IOException;

/**
 * Test helper that extends PdfResponseConverter to expose the protected writeInternal method
 * for testing purposes.
 */
public class TestPdfResponseConverter extends PdfResponseConverter {
    
    /**
     * Public wrapper around the protected writeInternal method for testing.
     */
    public void writeToOutput(PdfOutput pdfOutput, HttpOutputMessage outputMessage) throws IOException {
        writeInternal(pdfOutput, outputMessage);
    }
}
