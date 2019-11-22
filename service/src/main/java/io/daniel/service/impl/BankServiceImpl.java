package io.daniel.service.impl;

import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
import io.daniel.model.Money;
import io.daniel.service.AccountService;
import io.daniel.service.BankService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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

        List<LockAndCounter> lockAndCounters = extractLocks(sortedIds);
        lockAndCounters.stream().map(LockAndCounter::getLock).forEach(Lock::lock);
        try {
            Account from = accountService.getAccount(fromAcctId);
            Account to = accountService.getAccount(toAcctId);

            BigDecimal valueAfterConversion;
            if (from.hasTheSameCurrencyCode(amount.getCurrencyCode())) {
                valueAfterConversion = amount.getValue();
            } else {
                valueAfterConversion = convertCurrency(from.getCurrencyCode(), amount.getCurrencyCode(), amount.getValue());
            }

            if (!from.hasEnoughMoney(valueAfterConversion)) {
                throw new InsufficientFundsException("Not enough funds in the account " + from.getId());
            }
            from.debit(amount.getValue());

            if (from.hasTheSameCurrencyCode(to)) {
                to.credit(valueAfterConversion);
            } else {
                BigDecimal convertedAmount = convertCurrency(from.getCurrencyCode(), to.getCurrencyCode(), valueAfterConversion);
                to.credit(convertedAmount);
            }

            accountService.update(Arrays.asList(from, to));
        } finally {
            lockAndCounters.stream().map(LockAndCounter::getLock).forEach(Lock::unlock);
            sortedIds.forEach(this::cleanupLock);
        }
        log.info(String.format("Transfer money from %s to %s was succeeded.", fromAcctId, toAcctId));
    }

    private static final Map<Integer, LockAndCounter> locksMap = new ConcurrentHashMap<>();

    private static class LockAndCounter {
        private final Lock lock = new ReentrantLock();
        private final AtomicInteger counter = new AtomicInteger(0);

        public LockAndCounter() {
        }

        public AtomicInteger getCounter() {
            return counter;
        }

        public Lock getLock() {
            return lock;
        }
    }

    private List<LockAndCounter> extractLocks(List<Integer> sortedIds) {
        List<LockAndCounter> locks = new ArrayList<>();
        sortedIds.forEach(id -> locks.add(getLock(id)));
        return locks;
    }

    private LockAndCounter getLock(Integer key) {
        return locksMap.compute(key, (k, lockAndCounterInner) ->
        {
            if (lockAndCounterInner == null) {
                lockAndCounterInner = new LockAndCounter();
            }
            lockAndCounterInner.getCounter().incrementAndGet();
            return lockAndCounterInner;
        });
    }

    private void cleanupLock(Integer key) {
        LockAndCounter lockAndCounter = locksMap.get(key);
        if (lockAndCounter == null) {
            return;
        }
        if (lockAndCounter.counter.decrementAndGet() == 0) {
            locksMap.compute(key, (k, lockAndCounterInner) ->
            {
                if (lockAndCounterInner == null || lockAndCounterInner.counter.get() == 0) {
                    return null;
                }
                return lockAndCounterInner;
            });
        }
    }

    private BankServiceImpl() {
    }
}
