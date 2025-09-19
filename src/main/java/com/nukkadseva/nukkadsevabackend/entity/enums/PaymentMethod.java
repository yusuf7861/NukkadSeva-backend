package com.nukkadseva.nukkadsevabackend.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking"),
    UPI("UPI"),
    CASH_AFTER_SERVICE("Cash After Service");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
}
