package io.github.gzsombor.pdfserver.api;

import org.thymeleaf.context.Context;

/**
 * Interface to mark a {@link PdfOutput} to be further customize the Thmeleaf
 * {@link Context}
 * 
 * @author zsombor
 *
 */
public interface PdfContextConfigurer {

    void init(Context thymeleafContext);

}
