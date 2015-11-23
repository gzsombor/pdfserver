package org.vermillionalbatross.pdfserver.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.vermillionalbatross.pdfserver.api.PdfOutput;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.DocumentException;


@Component
public class PdfResponseConverter extends ThymeleafMessageConverter {
    private final static Logger LOG = LoggerFactory.getLogger(PdfResponseConverter.class);

    public static final MediaType PDF_MEDIA = new MediaType("application", "pdf");

    private Function<String, String> contentFormatter;

    public PdfResponseConverter() {
        super(PDF_MEDIA);
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
    protected void writeInternal(PdfOutput t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        LOG.info("rendering content  : {}", t);
        final String content = process(t);

        writePdf(outputMessage, content, false, t.getOutputName());
    }

    private void writePdf(HttpOutputMessage outputMessage, final String content, boolean forDownload, String name) throws IOException {
        outputMessage.getHeaders().setContentType(PDF_MEDIA);
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
