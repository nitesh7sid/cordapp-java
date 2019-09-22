package com.template.contracts;

import com.template.model.InvoiceStatus;
import com.template.model.PaymentStatus;
import com.template.states.InvoiceState;
import com.template.states.PaymentConfirmationState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TimeWindow;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import java.security.PublicKey;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;


public class InvoiceContract implements Contract {

    public static final String ID = "com.template.contracts.InvoiceContract";

    @Override
    public void verify(LedgerTransaction tx) {

        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        final Set<PublicKey> setOfSigners = new HashSet<>(command.getSigners());
        if (commandData instanceof Commands.Issue) {
            verifyIssue(tx, setOfSigners);
        } else if (commandData instanceof Commands.RaiseFunds) {
            verifyRaiseFunds(tx, setOfSigners);
        } else if (commandData instanceof Commands.ExitInvoice) {
            verifyExitInvoice(tx, setOfSigners);
        }
        else {
            throw new IllegalArgumentException("Unrecognised command.");
        }
    }

    private void verifyIssue(LedgerTransaction tx, Set<PublicKey> signers) {

        requireThat(req -> {
            req.using("No inputs should be consumed when issuing the invoice.", tx.getInputStates().isEmpty());
            // there can be multiple but for now lets stick to 1 for simplicity
            InvoiceState invoiceState = (InvoiceState) tx.getOutputStates().get(0);
            req.using("Only one invoice state should be created when issuing an invoice.", tx.getOutputStates().size() == 1);
            req.using("Buyer and Seller should be different parties", !invoiceState.getBuyer().equals(invoiceState.getSeller()));
            req.using("Invoice status should be ISSUED when creating new invoice", invoiceState.getInvoiceStatus() == InvoiceStatus.ISSUED);
            req.using("Invoice state should be shared only between buyer and seller", invoiceState.getParticipants().size() == 3 && invoiceState.getParticipants().containsAll(Arrays.asList(invoiceState.getBuyer(), invoiceState.getSeller(), invoiceState.getBank())));
           // Add more checks

            req.using("Invoice id must not be null", invoiceState.getInvoiceProperties().getInvoiceId() != null);
            req.using("Invoice term must be greater than zero", invoiceState.getInvoiceProperties().getTerm() > 0);
            final TimeWindow timeWindow = tx.getTimeWindow();
            req.using("Invoice date should not be in the past", invoiceState.getInvoiceProperties().getInvoiceDate().atStartOfDay().toInstant(ZoneOffset.UTC) .isAfter(timeWindow.getFromTime()));
            req.using("Pay date should be in the past", invoiceState.getInvoiceProperties().getPayDate().atStartOfDay().toInstant(ZoneOffset.UTC).isAfter(timeWindow.getFromTime()));
            req.using("Invoice must be generated on non-zero list of items", invoiceState.getInvoiceProperties().getLineItems().size() > 0);
            return null;
        });
    }

    private void verifyExitInvoice(LedgerTransaction tx, Set<PublicKey> signers) {

        requireThat(req -> {
            req.using("One input should be consumed when sending payment confirmation to buyer", tx.getInputStates().size() == 1);
            InvoiceState invoiceState = (InvoiceState) tx.getInputStates().get(0);PaymentConfirmationState paymentConfirmationState = (PaymentConfirmationState) tx.getOutputStates().get(0);
            req.using("Only one output should be created.", tx.getOutputStates().size() == 1);
            req.using("Bank must be in the signers list", signers.containsAll(Arrays.asList(invoiceState.getBank())));
            // validation for invoice properties
            return null;
        });
    }

    private void verifyRaiseFunds(LedgerTransaction tx, Set<PublicKey> signers) {

        requireThat(req->{
            req.using("Only one input should be consumed when raising funds against invoice", tx.getInputStates().size() == 1);
            req.using("Only one output should be produced when raising funds against invoice", tx.getOutputStates().size() == 1);

            InvoiceState inputInvoiceState = (InvoiceState) tx.getInputStates().get(0);
            InvoiceState outputInvoiceState = (InvoiceState) tx.getOutputStates().get(0);
            Party bank = tx.commandsOfType(Commands.RaiseFunds.class).get(0).getValue().bank;

            req.using("Input invoice state should be in ISSUED status.", inputInvoiceState.getInvoiceStatus() == InvoiceStatus.ISSUED);
            req.using("Invoice status should be REQUESTED_FOR_FUNDS sending invoice to bank for funds", outputInvoiceState.getInvoiceStatus() == InvoiceStatus.REQUESTED_FOR_FUNDS);
            req.using("Buyer should sign the transaction", signers.containsAll(Arrays.asList(inputInvoiceState.getBuyer())));
            req.using("Invoice state should be shared between buyer and bank", outputInvoiceState.getParticipants().size() == 2 && outputInvoiceState.getParticipants().containsAll(Arrays.asList(outputInvoiceState.getBuyer(), bank)));
            // validation for mismatch invoice properties

            return null;
        });

    }

    public interface Commands extends CommandData {
        class Issue implements InvoiceContract.Commands {
        }

        class ExitInvoice implements InvoiceContract.Commands {
        }
        class RaiseFunds implements InvoiceContract.Commands {
            private Party bank;
            public RaiseFunds(Party bank) {
                this.bank = bank;
            }
            public Party getBank() {
                return bank;
            }
        }
    }
}
