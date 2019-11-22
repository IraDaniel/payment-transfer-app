package io.daniel.model;

import java.math.BigDecimal;

public class DollarAmount extends Money {

    public DollarAmount(BigDecimal amount) {
        super(amount, CurrencyCode.USD);
    }
}
