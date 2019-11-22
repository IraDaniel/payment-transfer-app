package io.daniel;


import io.daniel.model.Account;
import io.daniel.model.DollarAmount;
import io.daniel.model.Money;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestUtils {

    public static Account initTestAccount(BigDecimal balance) {
        return new Account(new DollarAmount(balance));
    }

    public static Account initTestAccount(int balance) {
        return new Account(new DollarAmount(new BigDecimal(balance)));
    }

    public static void assertMoney(Money expected, Money actual) {
        assertEquals(expected.getCurrencyCode(), actual.getCurrencyCode());
        assertTrue(expected.getValue().equals(actual.getValue()));
    }
}
