package com.template.dto;


public class PayBuyerDTO {
    public String linearId;
    public String offlinePaymentId;


    public PayBuyerDTO(String linearId,  String offlinePaymentId) {
        this.linearId = linearId;
        this.offlinePaymentId = offlinePaymentId;
    }

    public PayBuyerDTO() {

    }
    public String getLinearId() {
        return linearId;
    }

    public String getOfflinePaymentId() {
        return offlinePaymentId;
    }

    @Override
    public String toString() {
        return linearId+ " " + offlinePaymentId ;
    }
}
