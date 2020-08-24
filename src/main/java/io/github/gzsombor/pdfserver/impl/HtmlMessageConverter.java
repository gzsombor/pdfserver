package io.github.gzsombor.pdfserver.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import io.github.gzsombor.pdfserver.api.MergedPdfOutput;
import io.github.gzsombor.pdfserver.api.PdfOutput;

@Component
public class HtmlMessageConverter extends ThymeleafMessageConverter {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlMessageConverter.class);

    public HtmlMessageConverter() {
        super(MediaType.TEXT_HTML);
    }

    @Override
    protected void writeInternal(PdfOutput t, HttpOutputMessage outputMessage) throws IOException {
        LOG.info("rendering content  : {}", t);
        if (t instanceof MergedPdfOutput) {
            MergedPdfOutput merged = (MergedPdfOutput) t;
            Collection<? extends PdfOutput> parts = merged.getIndividualPdfs();
            Document document = processList(parts);

            try {
                String content = documentToString(document);
                writeAsHtml(outputMessage, content);
            } catch (TransformerException e) {
                throw new IOException("Error transforming document: " + e.getMessage(), e);
            }
        } else {
            String content = process(t);

            writeAsHtml(outputMessage, content);
        }
    }

    private void writeAsHtml(HttpOutputMessage outputMessage, final String content) throws IOException {
        outputMessage.getHeaders().setContentType(MediaType.TEXT_HTML);
        final OutputStreamWriter w = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
        w.write(content);
        w.flush();
    }
}
