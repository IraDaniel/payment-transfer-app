package io.daniel.service;

import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
import io.daniel.model.DollarAmount;
import io.daniel.service.impl.AccountServiceImpl;
import io.daniel.service.impl.BankServiceImpl;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.daniel.TestUtils.initTestAccount;


public class BankServiceTest {

    private static final BankService bankService = BankServiceImpl.getInstance();
    private static final AccountService accountService = AccountServiceImpl.getInstance();

    @Test
    public void shouldTransferMoney() throws InterruptedException {
        Account from = initTestAccount(new BigDecimal(123));
        Account to = initTestAccount(new BigDecimal(123));
        from.setId(accountService.createNewAccount(from));
        to.setId(accountService.createNewAccount(to));

        bankService.transferMoney(0, 1, new DollarAmount(new BigDecimal(23)));

        Assert.assertEquals(from.getBalance().getValue(), new BigDecimal(100));
        Assert.assertEquals(to.getBalance().getValue(), new BigDecimal(146));
    }

    @Test(expected = InsufficientFundsException.class)
    public void shouldNotTransferMoney__balanceIsLessThanAmount() throws InterruptedException {
        Account from = initTestAccount(new BigDecimal(23));
        Account to = initTestAccount(new BigDecimal(123));
        from.setId(accountService.createNewAccount(from));
        to.setId(accountService.createNewAccount(to));

        bankService.transferMoney(0, 1, new DollarAmount(new BigDecimal(26)));
    }

    @Test
    public void shouldTransferMoneyInParallel() throws Exception {
        int numAccounts = 5;
        int numThreads = 20;
        int numIterations = 1000000;
        Random rnd = new Random();
        List<Integer> accountIds = new ArrayList<>();
        for (int i = 0; i < numAccounts; i++) {
            accountIds.add(accountService.createNewAccount(initTestAccount(new BigDecimal(rnd.nextInt()))));
        }
        class TransferThread extends Thread {
            public void run() {
                for (int i = 0; i < numIterations; i++) {
                    int fromAcct = rnd.nextInt(numAccounts);
                    int toAcct = rnd.nextInt(numAccounts);
                    try {
                        bankService.transferMoney(accountIds.get(fromAcct), accountIds.get(toAcct),
                                new DollarAmount(new BigDecimal(rnd.nextInt())));
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
        for (int i = 0; i < numThreads; i++)
            new TransferThread().start();
    }
}
