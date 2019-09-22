package com.template.dto;


public class RequestForFundsDTO {
    public String linearId;
    public String bank;


    public RequestForFundsDTO(String linearId,  String bank) {
        this.linearId = linearId;
        this.bank = bank;
    }

    public RequestForFundsDTO() {

    }
    public String getLinearId() {
        return linearId;
    }

    public String getBank() {
        return bank;
    }


    @Override
    public String toString() {
        return linearId+ " " + bank ;
    }
}
