package io.daniel.model;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Money {
    private BigDecimal value;
    private CurrencyCode currencyCode;
}
