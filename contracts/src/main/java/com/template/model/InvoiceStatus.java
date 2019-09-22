package com.template.model;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum InvoiceStatus {
    ISSUED,
    REQUESTED_FOR_FUNDS
}
