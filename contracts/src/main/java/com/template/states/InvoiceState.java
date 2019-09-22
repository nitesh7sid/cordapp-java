package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.InvoiceContract;
import com.template.model.InvoiceProperties;
import com.template.model.InvoiceStatus;
import com.template.schema.InvoiceSchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(InvoiceContract.class)
public class InvoiceState implements LinearState, QueryableState {

    private final Party seller;
    private final Party buyer;
    private final Party bank;
    private final InvoiceProperties invoiceProperties;
    private final InvoiceStatus invoiceStatus ;
    private final UniqueIdentifier linearId;

    @ConstructorForDeserialization
    public InvoiceState(Party seller, Party buyer, Party bank, InvoiceProperties invoiceProperties, InvoiceStatus invoiceStatus, UniqueIdentifier linearId) {

        this.seller = seller;
        this.buyer = buyer;
        this.bank = bank;
        this.invoiceProperties = invoiceProperties;
        this.invoiceStatus = invoiceStatus;
        this.linearId = linearId;
    }

    public Party getSeller() { return seller; }
    public InvoiceStatus getInvoiceStatus() { return invoiceStatus; }

    public InvoiceProperties getInvoiceProperties() { return invoiceProperties; }
    public Party getBank() { return bank; }
    public Party getBuyer() { return buyer; }

    @Override public UniqueIdentifier getLinearId() { return linearId; }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(buyer, seller, bank);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof InvoiceSchemaV1) {
            return new InvoiceSchemaV1.PersistentInvoice(
                    this.seller.getName().toString(),
                    this.buyer.getName().toString(),
                    this.invoiceProperties.getInvoiceId(),
                    this.invoiceProperties.getAmount(),
                    this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new InvoiceSchemaV1());
    }

    @Override
    public String toString() {
        return String.format("InvoiceState(sender=%s, borrower=%s, linearId=%s)", seller, buyer, linearId);
    }

    public InvoiceState withNewBank(Party bank) {
        return new InvoiceState(this.getSeller(), this.getBuyer(), bank, this.getInvoiceProperties(), InvoiceStatus.REQUESTED_FOR_FUNDS, this.getLinearId());
    }
}
