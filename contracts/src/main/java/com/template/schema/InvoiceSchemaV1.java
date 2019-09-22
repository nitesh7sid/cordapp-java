package com.template.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Amount;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Currency;
import java.util.UUID;

public class InvoiceSchemaV1 extends MappedSchema {

    public InvoiceSchemaV1() {
        super(InvoiceSchema.class, 1, ImmutableList.of(PersistentInvoice.class));
    }

    @Entity
    @Table(name = "invoice_states")
    public static class PersistentInvoice extends PersistentState {

        private final String seller;
        private final String borrower;
        private final String invoiceId;
        private final Long amount;;
        private final UUID linearId;

        public PersistentInvoice(String seller, String borrower, String invoiceId, Amount<Currency> amount, UUID linearId) {
            this.seller = seller;
            this.borrower = borrower;
            this.invoiceId = invoiceId;
            this.amount = amount.getQuantity();
            this.linearId = linearId;
        }

        public PersistentInvoice() {
            this.seller = null;
            this.borrower = null;
            this.invoiceId = null;
            this.amount = 0L;
            this.linearId = null;
        }

        public Long getAmount() {
            return amount;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public String getBorrower() {
            return borrower;
        }

        public UUID getLinearId() {
            return linearId;
        }

        public String getSeller() {
            return seller;
        }
    }
}
