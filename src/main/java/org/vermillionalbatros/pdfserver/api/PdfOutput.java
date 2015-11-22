package org.vermillionalbatros.pdfserver.api;

/**
 * Interface to mark an object convertable to PDF. Returns the template name,
 * and the name of the generated file.
 * 
 * @author zsombor
 *
 */
public interface PdfOutput {
    String getTemplateName();

    String getOutputName();

}

