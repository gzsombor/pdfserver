package io.github.gzsombor.pdfserver.test.model;

import io.github.gzsombor.pdfserver.api.PdfContextConfigurer;
import io.github.gzsombor.pdfserver.api.PdfOutput;
import org.thymeleaf.context.Context;

/**
 * Simple test model representing a report section.
 */
public class ReportSection implements PdfOutput, PdfContextConfigurer {
    private String title;
    private String content;
    private String sectionNumber;

    public ReportSection() {
    }

    public ReportSection(String sectionNumber, String title, String content) {
        this.sectionNumber = sectionNumber;
        this.title = title;
        this.content = content;
    }

    @Override
    public String getTemplateName() {
        return "report-section";
    }

    @Override
    public String getOutputName() {
        return "section-" + sectionNumber;
    }

    @Override
    public void init(Context thymeleafContext) {
        thymeleafContext.setVariable("sectionNumber", sectionNumber);
        thymeleafContext.setVariable("title", title);
        thymeleafContext.setVariable("content", content);
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }
}
