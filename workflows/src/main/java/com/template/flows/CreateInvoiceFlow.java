package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.InvoiceContract;
import com.template.model.InvoiceProperties;
import com.template.model.InvoiceStatus;
import com.template.states.InvoiceState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.security.PublicKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;


public class CreateInvoiceFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<String> {

        private final Party seller;
        private final Party buyer;
        private final InvoiceProperties invoiceProperties;
        private final Party regulator;

        private final Step INITIALISING = new Step("Performing initial steps.");
        private final Step BUILDING = new Step("Performing initial steps.");
        private final Step SIGNING = new Step("Signing transaction.");
        private final Step COLLECTING = new Step("Collecting counterparty signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final Step FINALISING = new Step("Finalising transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                INITIALISING, BUILDING, SIGNING, COLLECTING, FINALISING
        );

        public Initiator(Party seller, Party buyer, InvoiceProperties invoiceProperties, Party regulator) {
            this.seller = seller;
            this.buyer = buyer;
            this.invoiceProperties = invoiceProperties;
            this.regulator = regulator;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public String call() throws FlowException {
            // Initiator flow logic goes here.

            // Step 1. Initialisation.
            progressTracker.setCurrentStep(INITIALISING);
            final InvoiceState invoice = createInvoice();
            final PublicKey ourSigningKey = getOurIdentity().getOwningKey();
            // Step 2. Building.
            progressTracker.setCurrentStep(BUILDING);

            final TransactionBuilder utx = new TransactionBuilder(getFirstNotary())
                    .addOutputState(invoice, InvoiceContract.ID)
                    .addCommand(new InvoiceContract.Commands.Issue(),  ImmutableList.of(invoice.getSeller().getOwningKey(), invoice.getBuyer().getOwningKey()))
                    .setTimeWindow(getServiceHub().getClock().instant(), Duration.ofMinutes(5));

                utx.verify(getServiceHub());

            // Step 3. Sign the transaction. ImmutableList.of
            progressTracker.setCurrentStep(SIGNING);
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(utx);
            // Step 4. Get the counter-party signature.
            progressTracker.setCurrentStep(COLLECTING);
            final FlowSession buyerFlow = initiateFlow(buyer);
            final ImmutableSet<FlowSession> sessions = ImmutableSet.of(buyerFlow);
            final SignedTransaction stx = subFlow(new CollectSignaturesFlow(
                    ptx,
                    sessions,
                    COLLECTING.childProgressTracker()));

            // Step 5. Finalise the transaction.
            progressTracker.setCurrentStep(FINALISING);
            SignedTransaction finalTx = subFlow(new FinalityFlow(stx, sessions, FINALISING.childProgressTracker()));

            // send to observer
            subFlow(new ReportTransactionsToObserverFlows.ReportToObserver(finalTx, regulator));
            return invoice.getLinearId().toString();
        }

        @Suspendable
        private InvoiceState createInvoice() {
            return new InvoiceState(seller, buyer, buyer, invoiceProperties, InvoiceStatus.ISSUED, new UniqueIdentifier());
        }

        Party getFirstNotary() throws FlowException {
            List<Party> notaries = getServiceHub().getNetworkMapCache().getNotaryIdentities();
            if (notaries.isEmpty()) {
                throw new FlowException("No available notary.");
            }
            return notaries.get(0);
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartySession;

        public Responder(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();

            return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
        }
    }
}
