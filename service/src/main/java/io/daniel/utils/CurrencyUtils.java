package io.daniel.utils;

import io.daniel.model.CurrencyCode;

import java.math.BigDecimal;

/**
 * Created by Ira on 22.11.2019.
 */
public class CurrencyUtils {

    public static BigDecimal convertCurrency(CurrencyCode from, CurrencyCode to, BigDecimal value) {
        // in real, need to call an api which convert the value according to the exchange rate
        return value;
    }

    private CurrencyUtils() {
    }
}
