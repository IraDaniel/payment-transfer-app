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
import static io.daniel.utils.CurrencyUtils.convertValueToAccountCurrency;

public class BankServiceWithSynchronizeImpl implements BankService {
    private static AccountService accountService = AccountServiceImpl.getInstance();

    private static volatile BankServiceWithSynchronizeImpl instance;

    public static BankServiceWithSynchronizeImpl getInstance() {
        if (instance == null) {
            synchronized (BankServiceWithSynchronizeImpl.class) {
                if (instance == null) {
                    instance = new BankServiceWithSynchronizeImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void transferMoney(Integer fromAcctId, Integer toAcctId, Money amount) throws InterruptedException {
        notNull(fromAcctId, "Account ID to transfer money from is not specified");
        notNull(toAcctId, "Account ID to transfer money to is not specified");

        class Helper {
            public void transfer() throws InsufficientFundsException {
                Account fromAcct = accountService.getAccount(fromAcctId);
                Account toAcct = accountService.getAccount(toAcctId);
                BigDecimal valueAfterConversion = convertValueToAccountCurrency(amount, fromAcct.getCurrencyCode());

                if (!fromAcct.hasEnoughMoney(valueAfterConversion)) {
                    throw new InsufficientFundsException("Not enough funds in the account " + fromAcct.getId());
                }
                fromAcct.debit(valueAfterConversion);
                if (!fromAcct.hasTheSameCurrencyCode(toAcct)) {
                    BigDecimal convertedAmount = convertCurrency(fromAcct.getCurrencyCode(), toAcct.getCurrencyCode(), valueAfterConversion);
                    toAcct.credit(convertedAmount);
                } else {
                    toAcct.credit(valueAfterConversion);
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
