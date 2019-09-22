package com.template.dto;

public class PaySellerDTO {

    public String linearId;
    public String offlinePaymentId;
    public String seller;


    public PaySellerDTO(String linearId,  String offlinePaymentId) {
        this.linearId = linearId;
        this.offlinePaymentId = offlinePaymentId;
        this.seller = seller;

    }

    public PaySellerDTO() {

    }
    public String getLinearId() {
        return linearId;
    }


    public String getSeller() {
        return seller;
    }

    public String getOfflinePaymentId() {
        return offlinePaymentId;
    }

    @Override
    public String toString() {
        return linearId+ " " + offlinePaymentId + "seller";
    }
}
