package io.daniel.service;

import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
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


@Slf4j
public class BankServiceImpl implements BankService {
    private static BankServiceImpl instance;
    private static AccountService accountService = AccountService.getInstance();

    private Map<Integer, Lock> lockMap = new ConcurrentHashMap<>();

    private BankServiceImpl() {
    }

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
    public void transferMoney(Integer fromAcctId, Integer toAcctId, BigDecimal amount) throws InterruptedException {
        if (fromAcctId == null || toAcctId == null) {
            throw new IllegalArgumentException("Id is a required parameter");
        }
        log.info(String.format("Transfer money from %s to %s", fromAcctId, toAcctId));

        List<Integer> sortedIds = Stream.of(fromAcctId, toAcctId).sorted().collect(Collectors.toList());
        sortedIds.forEach(id -> lockMap.putIfAbsent(id, new ReentrantLock()));
        List<Lock> locks = sortedIds.stream().map(lockMap::get).collect(Collectors.toList());
        locks.forEach(Lock::lock);
        try {
            Account from = accountService.getAccount(fromAcctId);
            Account to = accountService.getAccount(toAcctId);
            if (!from.hasEnoughMoney(amount)) {
                throw new InsufficientFundsException("Not enough funds in the account " + from.getId());
            }
            from.debit(amount);
            to.credit(amount);
            accountService.update(Arrays.asList(from, to));
        } finally {
            locks.forEach(Lock::unlock);
        }

        log.info(String.format("Transfer money from %s to %s was succeeded.", fromAcctId, toAcctId));
        lockMap.remove(fromAcctId);
        lockMap.remove(toAcctId);
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
