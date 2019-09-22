package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableSet;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

public class ReportTransactionsToObserverFlows {

    @InitiatingFlow
    public static class ReportToObserver extends FlowLogic<Void> {

        private final SignedTransaction stx;
        private final Party regulator;
        private final ProgressTracker progressTracker = new ProgressTracker();

        public ReportToObserver(SignedTransaction stx, Party flowSession) {
            this.stx = stx;
            this.regulator = flowSession;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public Void call() {
            FlowSession flowSession = initiateFlow(regulator);
            flowSession.send(stx);
            return null;
        }
    }

    @InitiatedBy(ReportToObserver.class)
    public static class RecordStatesAsObserver extends FlowLogic<Void> {
        private final FlowSession otherFlow;

        public RecordStatesAsObserver(FlowSession otherFlow) {
            this.otherFlow = otherFlow;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction stx = otherFlow.receive(SignedTransaction.class).unwrap(it -> it);
            getServiceHub().recordTransactions(StatesToRecord.ALL_VISIBLE, ImmutableSet.of(stx));
            return null;
        }
    }
}
