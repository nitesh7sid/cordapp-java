package com.template.dto;

import com.template.model.LineItems;

import java.util.ArrayList;
import java.util.List;

    public class CreateInvoiceDTO {
        public String invoiceId;
        public String invoiceDate;
        public String payDate;
        public String term;
        public String amount;
        public String buyer;
        public ArrayList<LineItems> itemsList;


        public CreateInvoiceDTO(String invoiceId,  String invoiceDate, String payDate, String term, String amount, String buyer, ArrayList<LineItems> lineItems) {
            this.invoiceId = invoiceId;
            this.payDate = payDate;
            this.term = term;
            this.invoiceDate = invoiceDate;
            this.amount = amount;
            this.buyer = buyer;
            this.itemsList = lineItems;
        }

        public CreateInvoiceDTO() {

        }
        public String getInvoiceId() {
            return invoiceId;
        }

        public List<LineItems> getItemsList() {
            return itemsList;
        }

        public String getAmount() {
            return amount;
        }

        public String getInvoiceDate() {
            return invoiceDate;
        }

        public String getPayDate() {
            return payDate;
        }

        public String getTerm() {
            return term;
        }

        @Override
        public String toString() {
           return invoiceId+ " " + invoiceDate + " " +payDate;
        }
    }
//}
