package com.template.model;

import net.corda.core.contracts.Amount;
import net.corda.core.serialization.CordaSerializable;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@CordaSerializable
public class InvoiceProperties {

    private String invoiceId;
    private LocalDate invoiceDate;
    private Long term;
    private LocalDate payDate;
    private List<LineItems> lineItems;
    private Amount<Currency> amount;

    public List<LineItems> getLineItems() {
        return lineItems;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public Long getTerm() {
        return term;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Amount<Currency> getAmount() {
        return amount;
    }

    public InvoiceProperties(String invoiceId, LocalDate invoiceDate, Long term, LocalDate payDate, List<LineItems> lineItems, Amount<Currency> amount) {
        this.invoiceId = invoiceId;
        this.invoiceDate = invoiceDate;
        this.term = term;
        this.payDate = payDate;
        this.lineItems = lineItems;
        this.amount = amount;
    }

}
