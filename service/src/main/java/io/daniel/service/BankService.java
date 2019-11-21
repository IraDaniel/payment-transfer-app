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
public class BankService {
    private static BankService instance;
    private static AccountService accountService = AccountService.getInstance();

    private Map<Integer, Lock> lockMap = new ConcurrentHashMap<>();

    private BankService() {
    }

    public static BankService getInstance() {
        if (instance == null) {
            synchronized (BankService.class) {
                if (instance == null) {
                    instance = new BankService();
                }
            }
        }
        return instance;
    }

    public void transferMoney(Integer fromAccountId, Integer toAccountId, BigDecimal amount) throws InterruptedException {
        Account from = accountService.getAccount(fromAccountId);
        Account to = accountService.getAccount(toAccountId);

        transfer(from, to, amount);
    }

    /**
     * Transfer money from one account to another
     *
     * @param from
     * @param to
     * @param amount
     */
    public void transfer(Account from, Account to, BigDecimal amount) throws InterruptedException {
        if (from.getId() == null || to.getId() == null) {
            throw new IllegalArgumentException("Id is a required parameter");
        }
        log.info(String.format("Transfer money from %s to %s", from.getId(), to.getId()));
        // sort by id
        List<Account> accounts = Stream.of(from, to)
                .sorted((a1, a2) -> a1.getId().compareTo(a2.getId()))
                .collect(Collectors.toList());

        accounts.forEach(account -> lockMap.putIfAbsent(account.getId(), new ReentrantLock()));
        List<Lock> locks = accounts.stream()
                .map(account -> lockMap.get(account.getId()))
                .collect(Collectors.toList());
        locks.forEach(Lock::lock);
        try {
            System.out.println(lockMap.toString());
            if (from.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Not enough funds in the account " + from.getId());
            }
            from.debit(amount);
            to.credit(amount);
            accountService.update(Arrays.asList(from, to));
        } finally {
            locks.forEach(Lock::unlock);
        }

        log.info(String.format("Transfer money from %s to %s was succeeded.", from.getId(), to.getId()));
        lockMap.remove(from.getId());
        lockMap.remove(to.getId());
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
