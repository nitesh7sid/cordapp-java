package com.template.contracts;

import com.template.model.PaymentStatus;
import com.template.states.InvoiceState;
import com.template.states.PaymentConfirmationState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;


public class PaymentConfirmationContract implements Contract {

    public static final String ID = "com.template.contracts.PaymentConfirmationContract";

    @Override
    public void verify(LedgerTransaction tx) {

        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        final Set<PublicKey> setOfSigners = new HashSet<>(command.getSigners());
        if (commandData instanceof Commands.SendConfirmationToBuyer) {
            verifySendConfirmationToBuyer(tx, setOfSigners);
        } else if (commandData instanceof Commands.SendConfirmationToSeller) {
            verifySendConfirmationToSeller(tx, setOfSigners);
        } else {
            throw new IllegalArgumentException("Unrecognised command.");
        }
    }

    private void verifySendConfirmationToBuyer(LedgerTransaction tx, Set<PublicKey> signers) {

        requireThat(req -> {
            req.using("One input should be consumed when sending payment confirmation to buyer", tx.getInputStates().size() == 1);

            InvoiceState invoiceState = (InvoiceState) tx.getInputStates().get(0);
            PaymentConfirmationState paymentConfirmationState = (PaymentConfirmationState) tx.getOutputStates().get(0);
            req.using("Only one output should be created.", tx.getOutputStates().size() == 1);
            req.using("Sender and Receiver should be different parties", !paymentConfirmationState.getSender().equals(paymentConfirmationState.getReceiver()));
            req.using("Status should be BUYER_PAID", paymentConfirmationState.getStatus().equals(PaymentStatus.BUYER_PAID));
            req.using("Offline payment transaction id must not be null", paymentConfirmationState.getOfflinePaymentReferenceId() != null);
            req.using("Payment should be made for requested invoice only", paymentConfirmationState.getInvoiceId().equals(invoiceState.getInvoiceProperties().getInvoiceId()));
            req.using("Bank must be in the signers list", signers.containsAll(Arrays.asList(paymentConfirmationState.getSender())));
            req.using("Payment state should be shared only between buyer and bank", paymentConfirmationState.getParticipants().size() == 2 && paymentConfirmationState.getParticipants().containsAll(Arrays.asList(paymentConfirmationState.getSender(), paymentConfirmationState.getReceiver())));
            // validation for invoice properties
            return null;
        });
    }

    private void verifySendConfirmationToSeller(LedgerTransaction tx, Set<PublicKey> signers) {

        requireThat(req->{
            req.using("One input should be consumed when sending payment confirmation to seller", tx.getInputStates().size() == 1);
            PaymentConfirmationState inputPaymentConfirmationState = (PaymentConfirmationState) tx.getInputStates().get(0);
            PaymentConfirmationState outputPaymentConfirmationState = (PaymentConfirmationState) tx.getOutputStates().get(0);
            req.using("Only one output should be created.", tx.getOutputStates().size() == 1);
            req.using("Buyer should be paid before by bank", inputPaymentConfirmationState.getStatus() == PaymentStatus.BUYER_PAID);
            req.using("Sender and Receiver should be different parties", !outputPaymentConfirmationState.getSender().equals(outputPaymentConfirmationState.getReceiver()));
            req.using("Status should be SELLER_PAID", outputPaymentConfirmationState.getStatus().equals(PaymentStatus.SELLER_PAID));
            req.using("Offline payment transaction id must not be null", outputPaymentConfirmationState.getOfflinePaymentReferenceId() != null);
            req.using("Payment should be made for requested invoice only", outputPaymentConfirmationState.getInvoiceId().equals(inputPaymentConfirmationState.getInvoiceId()));
            req.using("Buyer must be in the signers list", signers.containsAll(Arrays.asList(inputPaymentConfirmationState.getReceiver())));
            req.using("Payment state should be shared only between buyer and seller", outputPaymentConfirmationState.getParticipants().size() == 2 && outputPaymentConfirmationState.getParticipants().containsAll(Arrays.asList(inputPaymentConfirmationState.getReceiver(), outputPaymentConfirmationState.getReceiver())));
            // validation for invoice properties
            return null;
        });

    }

    public interface Commands extends CommandData {
        class SendConfirmationToBuyer implements PaymentConfirmationContract.Commands {
        }

        class SendConfirmationToSeller implements PaymentConfirmationContract.Commands {
        }
    }
}
