package org.vermillionalbatross.pdfserver.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.vermillionalbatross.pdfserver.api.PdfOutput;

@Component
public class HtmlMessageConverter extends ThymeleafMessageConverter {
    private final static Logger LOG = LoggerFactory.getLogger(HtmlMessageConverter.class);

    public HtmlMessageConverter() {
        super(MediaType.TEXT_HTML);
    }

    @Override
    protected void writeInternal(PdfOutput t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        LOG.info("rendering content  : {}", t);
        String content = process(t);

        writeAsHtml(outputMessage, content);
    }

    private void writeAsHtml(HttpOutputMessage outputMessage, final String content) throws IOException {
        outputMessage.getHeaders().setContentType(MediaType.TEXT_HTML);
        final OutputStreamWriter w = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
        w.write(content);
        w.flush();
    }

}
