package io.daniel.model;

import java.math.BigDecimal;


public class MoneyAmount {
    private final BigDecimal value;
    private final String currencyCode;

    public BigDecimal getValue() {
        return value;
    }

    public MoneyAmount(BigDecimal value, String currencyCode) {
        this.value = value;
        this.currencyCode = currencyCode;
    }
}
