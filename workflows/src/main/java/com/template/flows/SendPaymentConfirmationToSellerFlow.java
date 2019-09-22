package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.InvoiceContract;
import com.template.contracts.PaymentConfirmationContract;
import com.template.model.PaymentStatus;
import com.template.states.PaymentConfirmationState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class SendPaymentConfirmationToSellerFlow {

    @InitiatingFlow
    @StartableByRPC

    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final UniqueIdentifier paymentConfirmationStateId;
        private final String offlinePaymentReferenceId;
        private final Party seller;
        private final Party regulator;

        public Initiator(UniqueIdentifier invoiceLinearId, Party regulator, String offlinePaymentReferenceId, Party seller) {
            this.paymentConfirmationStateId = invoiceLinearId;
            this.offlinePaymentReferenceId = offlinePaymentReferenceId;
            this.seller = seller;
            this.regulator = regulator;
        }

        private final ProgressTracker.Step INITIALISING = new ProgressTracker.Step("Performing initial steps.");
        private final ProgressTracker.Step BUILDING = new ProgressTracker.Step("Performing initial steps.");
        private final ProgressTracker.Step SIGNING = new ProgressTracker.Step("Signing transaction.");
        private final ProgressTracker.Step COLLECTING = new ProgressTracker.Step("Collecting counterparty signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final ProgressTracker.Step FINALISING = new ProgressTracker.Step("Finalising transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                INITIALISING, BUILDING, SIGNING, COLLECTING, FINALISING
        );

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Initiator flow logic goes here.

            // Step 1. Initialisation.
            progressTracker.setCurrentStep(INITIALISING);
            final StateAndRef<PaymentConfirmationState> paymentConfirmationStateAndRef = getPaymentConfirmationByLinearId(paymentConfirmationStateId);
            final PaymentConfirmationState paymentConfirmationState = paymentConfirmationStateAndRef.getState().getData();
            final PaymentConfirmationState outputPaymentConfirmationState = new PaymentConfirmationState(paymentConfirmationState.getReceiver(), seller, paymentConfirmationState.getInvoiceId(), offlinePaymentReferenceId, PaymentStatus.SELLER_PAID, new UniqueIdentifier());
            final PublicKey ourSigningKey = paymentConfirmationState.getReceiver().getOwningKey();
            FlowSession sellerSession = initiateFlow(seller);

            // Step 2. Building.
            progressTracker.setCurrentStep(BUILDING);

            final TransactionBuilder utx = new TransactionBuilder(getFirstNotary())
                    .addInputState(paymentConfirmationStateAndRef)
                    .addOutputState(outputPaymentConfirmationState, InvoiceContract.ID)
                    .addCommand(new PaymentConfirmationContract.Commands.SendConfirmationToSeller(), Arrays.asList(ourSigningKey))
                    .setTimeWindow(getServiceHub().getClock().instant(), Duration.ofMinutes(5));

            utx.verify(getServiceHub());
            // Step 3. Sign the transaction.
            progressTracker.setCurrentStep(SIGNING);
            final SignedTransaction stx = getServiceHub().signInitialTransaction(utx, ourSigningKey);


            // Step 5. Finalise the transaction.
            progressTracker.setCurrentStep(FINALISING);
            SignedTransaction finalTx = subFlow(new FinalityFlow(stx, ImmutableSet.of(sellerSession), FINALISING.childProgressTracker()));
            subFlow(new ReportTransactionsToObserverFlows.ReportToObserver(finalTx, regulator));
            return finalTx;
        }

        StateAndRef<PaymentConfirmationState> getPaymentConfirmationByLinearId(UniqueIdentifier linearId) throws FlowException {
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                    null,
                    ImmutableList.of(linearId),
                    Vault.StateStatus.UNCONSUMED,
                    null);

            List<StateAndRef<PaymentConfirmationState>> paymentStates = getServiceHub().getVaultService().queryBy(PaymentConfirmationState.class, queryCriteria).getStates();
            if (paymentStates.size() != 1) {
                throw new FlowException(String.format("invoice with id %s not found.", linearId));
            }
            return paymentStates.get(0);
        }

        Party getFirstNotary() throws FlowException {
            List<Party> notaries = getServiceHub().getNetworkMapCache().getNotaryIdentities();
            if (notaries.isEmpty()) {
                throw new FlowException("No available notary.");
            }
            return notaries.get(0);
        }
    }

    @InitiatedBy(SendPaymentConfirmationToSellerFlow.Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {
        private final FlowSession otherFlow;

        public Responder(FlowSession otherFlow) {
            this.otherFlow = otherFlow;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(otherFlow));
        }
    }
}

