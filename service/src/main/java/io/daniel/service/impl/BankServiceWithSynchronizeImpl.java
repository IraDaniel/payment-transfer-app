package io.daniel.service.impl;

import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
import io.daniel.model.Money;
import io.daniel.service.AccountService;
import io.daniel.service.BankService;

import java.math.BigDecimal;
import java.util.Arrays;

import static io.daniel.utils.AssertionUtils.notNull;
import static io.daniel.utils.CurrencyUtils.convertCurrency;

public class BankServiceWithSynchronizeImpl implements BankService {
    private static AccountService accountService = AccountServiceImpl.getInstance();

    @Override
    public void transferMoney(Integer fromAcctId, Integer toAcctId, Money amount) throws InterruptedException {
        notNull(fromAcctId, "Account ID to transfer money from is not specified");
        notNull(toAcctId, "Account ID to transfer money to is not specified");

        class Helper {
            public void transfer() throws InsufficientFundsException {
                Account fromAcct = accountService.getAccount(fromAcctId);
                Account toAcct = accountService.getAccount(toAcctId);
                BigDecimal valueAfterConvertingToFromCurrency;
                if (fromAcct.hasTheSameCurrencyCode(amount.getCurrencyCode())) {
                    valueAfterConvertingToFromCurrency = amount.getValue();
                } else {
                    valueAfterConvertingToFromCurrency = convertCurrency(fromAcct.getCurrencyCode(), amount.getCurrencyCode(), amount.getValue());
                }

                if (!fromAcct.hasEnoughMoney(valueAfterConvertingToFromCurrency)) {
                    throw new InsufficientFundsException("Not enough funds in the account " + fromAcct.getId());
                }
                fromAcct.debit(valueAfterConvertingToFromCurrency);
                if (!fromAcct.hasTheSameCurrencyCode(toAcct)) {
                    BigDecimal convertedAmount = convertCurrency(fromAcct.getCurrencyCode(), toAcct.getCurrencyCode(), valueAfterConvertingToFromCurrency);
                    toAcct.credit(convertedAmount);
                } else {
                    toAcct.credit(valueAfterConvertingToFromCurrency);
                }
                accountService.update(Arrays.asList(fromAcct, toAcct));
            }

        }

        if (fromAcctId < toAcctId) {
            synchronized (fromAcctId) {
                synchronized (toAcctId) {
                    new Helper().transfer();
                }
            }
        } else {
            synchronized (toAcctId) {
                synchronized (fromAcctId) {
                    new Helper().transfer();
                }
            }
        }
    }
}
