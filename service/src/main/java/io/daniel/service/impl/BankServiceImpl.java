package io.daniel.service.impl;

import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
import io.daniel.model.Money;
import io.daniel.service.AccountService;
import io.daniel.service.BankService;
import io.daniel.utils.AssertionUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.daniel.utils.AssertionUtils.notNull;
import static io.daniel.utils.CurrencyUtils.convertCurrency;


@Slf4j
public class BankServiceImpl implements BankService {

    private static AccountService accountService = AccountServiceImpl.getInstance();

    private static volatile BankServiceImpl instance;

    public static BankServiceImpl getInstance() {
        if (instance == null) {
            synchronized (BankServiceImpl.class) {
                if (instance == null) {
                    instance = new BankServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void transferMoney(Integer fromAcctId, Integer toAcctId, Money amount) throws InterruptedException {
        notNull(fromAcctId, "Account ID to transfer money from is not specified");
        notNull(toAcctId, "Account ID to transfer money to is not specified");

        log.info(String.format("Transfer money from %s to %s", fromAcctId, toAcctId));

        List<Integer> sortedIds = Stream.of(fromAcctId, toAcctId).sorted().collect(Collectors.toList());
        sortedIds.forEach(id -> lockMap.putIfAbsent(id, new ReentrantLock()));
        List<Lock> locks = sortedIds.stream().map(lockMap::get).collect(Collectors.toList());
        locks.forEach(Lock::lock);
        try {
            Account from = accountService.getAccount(fromAcctId);
            Account to = accountService.getAccount(toAcctId);

            BigDecimal valueAfterConvertingToFromCurrency;
            if (from.hasTheSameCurrencyCode(amount.getCurrencyCode())) {
                valueAfterConvertingToFromCurrency = amount.getValue();
            } else {
                valueAfterConvertingToFromCurrency = convertCurrency(from.getCurrencyCode(), amount.getCurrencyCode(), amount.getValue());
            }

            if (!from.hasEnoughMoney(valueAfterConvertingToFromCurrency)) {
                throw new InsufficientFundsException("Not enough funds in the account " + from.getId());
            }
            from.debit(amount.getValue());

            if (from.hasTheSameCurrencyCode(to)) {
                to.credit(valueAfterConvertingToFromCurrency);
            } else {
                BigDecimal convertedAmount = convertCurrency(from.getCurrencyCode(), to.getCurrencyCode(), valueAfterConvertingToFromCurrency);
                to.credit(convertedAmount);
            }

            accountService.update(Arrays.asList(from, to));
        } finally {
            locks.forEach(Lock::unlock);
        }

        log.info(String.format("Transfer money from %s to %s was succeeded.", fromAcctId, toAcctId));
        lockMap.remove(fromAcctId);
        lockMap.remove(toAcctId);
    }

    private final Map<Integer, Lock> lockMap = new ConcurrentHashMap<>();

    private BankServiceImpl() {
    }
}
