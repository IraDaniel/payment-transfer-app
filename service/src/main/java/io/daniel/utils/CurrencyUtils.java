package io.daniel.utils;

import io.daniel.model.CurrencyCode;
import io.daniel.model.Money;

import java.math.BigDecimal;

public class CurrencyUtils {

    public static BigDecimal convertCurrency(CurrencyCode from, CurrencyCode to, BigDecimal value) {
        // in real, need to call an api which convert the value according to the exchange rate
        return value;
    }

    public static BigDecimal convertValueToAccountCurrency(Money amount, CurrencyCode accountCurrency) {
        BigDecimal valueAfterConversion;
        if (accountCurrency == amount.getCurrencyCode()) {
            valueAfterConversion = amount.getValue();
        } else {
            valueAfterConversion = convertCurrency(accountCurrency, amount.getCurrencyCode(), amount.getValue());
        }
        return valueAfterConversion;
    }

    private CurrencyUtils() {
    }
}
