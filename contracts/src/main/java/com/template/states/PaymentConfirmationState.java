package com.template.states;

import com.template.contracts.InvoiceContract;
import com.template.contracts.PaymentConfirmationContract;
import com.template.model.PaymentStatus;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(PaymentConfirmationContract.class)
public class PaymentConfirmationState implements LinearState {
    private final Party sender;
    private final Party receiver;
    private final String invoiceId;
    private final String offlinePaymentReferenceId;
    private final PaymentStatus status;
    private final UniqueIdentifier linearId;

    public PaymentConfirmationState(Party sender, Party receiver, String invoiceId, String offlinePaymentReferenceId, PaymentStatus status, UniqueIdentifier linearId) {
        this.sender =  sender;
        this.receiver = receiver;
        this.invoiceId = invoiceId;
        this.offlinePaymentReferenceId = offlinePaymentReferenceId;
        this.status = status;
        this.linearId = linearId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Party getReceiver() {
        return receiver;
    }

    public Party getSender() {
        return sender;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getOfflinePaymentReferenceId() {
        return offlinePaymentReferenceId;
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sender, receiver);
    }
}
