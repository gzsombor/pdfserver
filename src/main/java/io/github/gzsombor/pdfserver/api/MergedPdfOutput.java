package io.github.gzsombor.pdfserver.api;

import java.util.Collection;

public interface MergedPdfOutput extends PdfOutput {
    Collection<? extends PdfOutput> getIndividualPdfs();
}
