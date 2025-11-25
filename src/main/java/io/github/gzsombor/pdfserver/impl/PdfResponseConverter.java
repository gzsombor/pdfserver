package io.github.gzsombor.pdfserver.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import io.github.gzsombor.pdfserver.api.MergedPdfOutput;
import io.github.gzsombor.pdfserver.api.PdfOutput;
import org.xhtmlrenderer.pdf.ITextRenderer;

import org.openpdf.text.DocumentException;

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
        if (t instanceof MergedPdfOutput) {
            MergedPdfOutput merged = (MergedPdfOutput) t;
            Collection<? extends PdfOutput> parts = merged.getIndividualPdfs();
            Document document = processList(parts);

            writePdf(outputMessage, false, t.getOutputName(), renderer -> renderer.setDocument(document, null));
        } else {
            final String content = process(t);
    
            writePdf(outputMessage, false, t.getOutputName(), renderer -> renderer.setDocumentFromString(content));
        }
    }

    @Override
    protected String process(PdfOutput toPdf) {
        String content = super.process(toPdf);
        return contentFormatter != null ? contentFormatter.apply(content) : content;
    }

    private void writePdf(HttpOutputMessage outputMessage, boolean forDownload, String name, Consumer<ITextRenderer> setup) throws IOException {
        outputMessage.getHeaders().setContentType(MediaType.APPLICATION_PDF);
        if (forDownload && name != null) {
            outputMessage.getHeaders().set("Content-Disposition", "attachment; filename=\"" + name.replace('"', '_') + ".pdf\"");
        }
        final OutputStream out = outputMessage.getBody();

        try {
            final ITextRenderer renderer = new ITextRenderer();

            setup.accept(renderer);
            renderer.layout();
            renderer.createPDF(out);
            renderer.finishPDF();
        } catch (final DocumentException e) {
            LOG.error("Document error: " + e.getMessage(), e);
        }
    }
}
