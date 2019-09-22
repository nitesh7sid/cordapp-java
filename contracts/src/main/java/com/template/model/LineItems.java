package com.template.model;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class LineItems {

    public String itemName;
    public String itemCode;
    public String itemDescription;

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public LineItems(String itemName, String itemCode, String itemDescription) {

        this.itemName = itemName;
        this.itemCode = itemCode;
        this.itemDescription = itemDescription;
    }

    public LineItems() {

    }

}
