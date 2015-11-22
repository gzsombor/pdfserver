package org.vermillionalbatros.pdfserver.impl;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.vermillionalbatros.pdfserver.api.PdfContextConfigurer;
import org.vermillionalbatros.pdfserver.api.PdfOutput;

/**
 * Base class for executing thmeleaf process to convert the PdfOutput to textual
 * representation.
 * 
 * @author zsombor
 *
 */
public abstract class ThymeleafMessageConverter extends AbstractHttpMessageConverter<PdfOutput> {

    @Inject
    private SpringTemplateEngine templateEngine;

    private boolean alwaysReload = false;
    /**
     * 
     */
    public ThymeleafMessageConverter() {
        super();
    }

    /**
     * @param supportedMediaTypes
     */
    public ThymeleafMessageConverter(MediaType... supportedMediaTypes) {
        super(supportedMediaTypes);
    }

    /**
     * @param supportedMediaType
     */
    public ThymeleafMessageConverter(MediaType supportedMediaType) {
        super(supportedMediaType);
    }

    public void setAlwaysReload(boolean alwaysReload) {
        this.alwaysReload = alwaysReload;
    }

    public boolean isAlwaysReload() {
        return alwaysReload;
    }

    public SpringTemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return PdfOutput.class.isAssignableFrom(clazz);
    }

    @Override
    protected PdfOutput readInternal(Class<? extends PdfOutput> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new IllegalArgumentException();
    }

    protected String process(PdfOutput toPdf) {
        final Context context = new Context(Locale.ENGLISH);
        context.setVariable("record", toPdf);
        if (toPdf instanceof PdfContextConfigurer) {
            ((PdfContextConfigurer) toPdf).init(context);
        }

        if (templateEngine.isInitialized() && alwaysReload) {
            templateEngine.getTemplateRepository().clearTemplateCache();
        }

        final String content = templateEngine.process("pdf/" + toPdf.getTemplateName(), context);

        return content;
    }
}
