package io.github.gzsombor.pdfserver.test.model;

import io.github.gzsombor.pdfserver.api.PdfContextConfigurer;
import io.github.gzsombor.pdfserver.api.PdfOutput;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;

/**
 * Test model representing invoice data with various fields to test template rendering.
 */
public class InvoiceData implements PdfOutput, PdfContextConfigurer {
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String customerName;
    private String customerAddress;
    private List<InvoiceItem> items;
    private double totalAmount;

    public InvoiceData() {
    }

    public InvoiceData(String invoiceNumber, LocalDate invoiceDate, String customerName, 
                       String customerAddress, List<InvoiceItem> items) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.items = items;
        this.totalAmount = items.stream().mapToDouble(InvoiceItem::getTotal).sum();
    }

    @Override
    public String getTemplateName() {
        return "invoice";
    }

    @Override
    public String getOutputName() {
        return "invoice-" + invoiceNumber;
    }

    @Override
    public void init(Context thymeleafContext) {
        thymeleafContext.setVariable("invoiceNumber", invoiceNumber);
        thymeleafContext.setVariable("invoiceDate", invoiceDate);
        thymeleafContext.setVariable("customerName", customerName);
        thymeleafContext.setVariable("customerAddress", customerAddress);
        thymeleafContext.setVariable("items", items);
        thymeleafContext.setVariable("totalAmount", totalAmount);
    }

    // Getters and setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static class InvoiceItem {
        private String description;
        private int quantity;
        private double unitPrice;

        public InvoiceItem() {
        }

        public InvoiceItem(String description, int quantity, double unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public double getTotal() {
            return quantity * unitPrice;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
