package io.github.gzsombor.pdfserver.impl;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import io.github.gzsombor.pdfserver.api.PdfContextConfigurer;
import io.github.gzsombor.pdfserver.api.PdfOutput;

/**
 * Base class for executing thmeleaf process to convert the PdfOutput to textual
 * representation.
 * 
 * @author zsombor
 */
public abstract class ThymeleafMessageConverter extends AbstractHttpMessageConverter<PdfOutput> {
    @Inject
    private SpringTemplateEngine templateEngine;

    private boolean alwaysReload;

    private String pathPrefix = "";

    public ThymeleafMessageConverter() {
    }

    public ThymeleafMessageConverter(MediaType... supportedMediaTypes) {
        super(supportedMediaTypes);
    }

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

    public void setPathPrefix(String path) {
        this.pathPrefix = path;
    }

    public String getPathPrefix() {
        return this.pathPrefix;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return PdfOutput.class.isAssignableFrom(clazz);
    }

    @Override
    protected PdfOutput readInternal(Class<? extends PdfOutput> clazz, HttpInputMessage inputMessage) throws IOException {
        throw new IllegalArgumentException();
    }

    protected String process(PdfOutput toPdf) {
        final Context context = new Context(Locale.ENGLISH);
        context.setVariable("record", toPdf);
        if (toPdf instanceof PdfContextConfigurer) {
            ((PdfContextConfigurer) toPdf).init(context);
        }

        if (templateEngine.isInitialized() && alwaysReload) {
            templateEngine.getCacheManager().clearAllCaches();
        }

        return templateEngine.process(pathPrefix + toPdf.getTemplateName(), context);
    }
}
