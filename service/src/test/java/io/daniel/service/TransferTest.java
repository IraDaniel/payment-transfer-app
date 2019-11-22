package io.daniel.service;

import io.daniel.model.Account;

import java.math.BigDecimal;


public class TransferTest {

    Account from;
    Account to;
    BigDecimal amount;

    public TransferTest(Account from, Account to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }
}
