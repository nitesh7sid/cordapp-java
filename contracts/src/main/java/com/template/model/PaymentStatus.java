package com.template.model;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum PaymentStatus {
    BUYER_PAID,
    SELLER_PAID;
}
