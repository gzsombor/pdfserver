package io.github.gzsombor.pdfserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import jakarta.inject.Inject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;

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
    
    private XPathFactory xpathFactory = XPathFactory.newInstance();

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

    protected Document parseHtml(String content) {
        InputSource is = new InputSource(new BufferedReader(new StringReader(content)));
        Document dom = XMLResource.load(is).getDocument();
        return dom;
    }

    protected Document processList(Collection<? extends PdfOutput> toPdf) {
        try {
            Document document = null;
            String documentName = null;
            XPath path = xpathFactory.newXPath();
            XPathExpression bodyPath = path.compile("//body");
            for (PdfOutput pdfFragments : toPdf) {
                String content = process(pdfFragments);
                Document fragment = parseHtml(content);
                if (document == null) {
                    document = fragment;
                    documentName = pdfFragments.getTemplateName();
                } else {
                    mergeHtml(bodyPath, document, fragment, pdfFragments.getTemplateName());
                }
            }
            return document;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Unable to parse XPath expression:"+e.getMessage(), e);
        }
    }

    /**
     * @param bodyPath
     * @param document
     * @param fragment
     * @throws XPathExpressionException
     */
    private void mergeHtml(XPathExpression bodyPath, Document document, Document fragment, String templateName) throws XPathExpressionException {
        Node documentBody = (Node) bodyPath.evaluate(document, XPathConstants.NODE);
        if (documentBody == null) {
            throw new IllegalArgumentException("Unable to find 'body' element in : "+documentBody);
        }
        Node newBody = (Node) bodyPath.evaluate(fragment, XPathConstants.NODE);
        if (newBody == null) {
            throw new IllegalArgumentException("Unable to find 'body' element in template: "+templateName);
        }
        NodeList childNodes = newBody.getChildNodes();
        for (int i=0;i<childNodes.getLength();i++) {
            Node toAppend = childNodes.item(i).cloneNode(true);
            document.adoptNode(toAppend);
            documentBody.appendChild(toAppend);
        }
    }

    protected String documentToString(Document document) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(document), new StreamResult(sw));
        return sw.toString();
    }
}
