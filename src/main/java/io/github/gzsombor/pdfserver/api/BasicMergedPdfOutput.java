package io.github.gzsombor.pdfserver.api;

import java.util.Collection;
import java.util.List;

public class BasicMergedPdfOutput implements MergedPdfOutput {

    private final List<? extends PdfOutput> pdfs;
    private final String outputName;

    public BasicMergedPdfOutput(String outputName, List<? extends PdfOutput> pdfs) {
        this.pdfs = pdfs;
        this.outputName = outputName;
    }

    @Override
    public String getTemplateName() {
        throw new IllegalStateException("getTemplateName is not implemented");
    }

    @Override
    public String getOutputName() {
        return outputName;
    }

    @Override
    public Collection<? extends PdfOutput> getIndividualPdfs() {
        return pdfs;
    }

}
