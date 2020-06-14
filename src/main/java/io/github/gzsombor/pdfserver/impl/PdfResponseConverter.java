package io.github.gzsombor.pdfserver.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import io.github.gzsombor.pdfserver.api.PdfOutput;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.DocumentException;

@Component
public class PdfResponseConverter extends ThymeleafMessageConverter {
    private static final Logger LOG = LoggerFactory.getLogger(PdfResponseConverter.class);

    private Function<String, String> contentFormatter;

    public PdfResponseConverter() {
        super(MediaType.APPLICATION_PDF);
        this.contentFormatter = x -> x;
    }

    /**
     * Set the content formatter function, so it is possible to further
     * customize the generated content before it's fed into the PDF generator.
     * 
     * @param contentFormatter
     */
    public void setContentFormatter(Function<String, String> contentFormatter) {
        this.contentFormatter = contentFormatter;
    }

    @Override
    protected void writeInternal(PdfOutput t, HttpOutputMessage outputMessage) throws IOException {
        LOG.info("rendering content  : {}", t);
        final String content = process(t);

        writePdf(outputMessage, content, false, t.getOutputName());
    }

    private void writePdf(HttpOutputMessage outputMessage, final String content, boolean forDownload, String name) throws IOException {
        outputMessage.getHeaders().setContentType(MediaType.APPLICATION_PDF);
        if (forDownload && name != null) {
            outputMessage.getHeaders().set("Content-Disposition", "attachment; filename=\"" + name.replace('"', '_') + ".pdf\"");
        }
        final OutputStream out = outputMessage.getBody();

        try {
            final ITextRenderer renderer = new ITextRenderer();

            final String formatted = contentFormatter != null ? contentFormatter.apply(content) : content;

            renderer.setDocumentFromString(formatted);
            renderer.layout();
            renderer.createPDF(out);
            renderer.finishPDF();
        } catch (final DocumentException e) {
            LOG.error("Document error: " + e.getMessage(), e);
        }
    }
}
