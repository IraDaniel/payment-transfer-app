package io.daniel.model;

import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
public class DollarAmount extends Money {

    public DollarAmount(BigDecimal amount) {
        super(amount, CurrencyCode.USD);
    }
}
