package io.daniel.service;

import io.daniel.dao.impl.AccountDaoH2Impl;
import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
import io.daniel.model.DollarAmount;
import io.daniel.service.impl.AccountServiceImpl;
import io.daniel.service.impl.BankServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.SuppressCode.suppressConstructor;

@RunWith(PowerMockRunner.class)
public class BankServiceTest {

    private final BankService bankService = BankServiceImpl.getInstance();

    private final AccountService accountService = AccountServiceImpl.getInstance();


    private AccountDaoH2Impl accountDao;

    @Before
    public void init() {
        suppressConstructor(AccountDaoH2Impl.class);
        mockStatic(AccountDaoH2Impl.class);
        AccountDaoH2Impl accountDao = PowerMockito.mock(AccountDaoH2Impl.class);
        PowerMockito.when(AccountDaoH2Impl.getInstance()).thenReturn(accountDao);
    }

    @Test
    public void shouldTransferMoney() throws InterruptedException {
        Account from = initTestAccount(new BigDecimal(123));
        Account to = initTestAccount(new BigDecimal(123));
        when(accountDao.create(from)).thenReturn(0);
        when(accountDao.create(to)).thenReturn(1);
        from.setId(0);
        to.setId(1);
        when(accountDao.getById(0)).thenReturn(from);
        when(accountDao.getById(1)).thenReturn(to);

        bankService.transferMoney(0, 1, new BigDecimal(23));
        Assert.assertEquals(from.getBalance(), new BigDecimal(100));
        Assert.assertEquals(to.getBalance(), new BigDecimal(146));
    }

    @Test(expected = InsufficientFundsException.class)
    public void shouldNotTransferMoney__balanceIsLessThanAmount() throws InterruptedException {
        Account from = initTestAccount(new BigDecimal(23));
        Account to = initTestAccount(new BigDecimal(123));
        when(accountDao.create(from)).thenReturn(0);
        when(accountDao.create(to)).thenReturn(1);
        from.setId(0);
        to.setId(1);
        when(accountDao.getById(0)).thenReturn(from);
        when(accountDao.getById(1)).thenReturn(to);
        bankService.transferMoney(0, 1, new BigDecimal(26));
    }

    @Test
    public void shouldTransferMoneyInParallel() throws Exception {
        Account from = initTestAccount(new BigDecimal(100));
        Account to = initTestAccount(new BigDecimal(0));
        from.setId(accountDao.create(from));
        to.setId(accountDao.create(to));

        ExecutorService executor = Executors.newFixedThreadPool(5);
//        executor.execute(new TestThread(bankService, new Transfer(0, 1, new BigDecimal(26))));
//        executor.execute(new TestThread(bankService, new Transfer(1, 0, new BigDecimal(10))));
//        executor.execute(new TestThread(bankService, new Transfer(1, 0, new BigDecimal(10))));
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }

    private static final int NUM_THREADS = 20;
    private static final int NUM_ACCOUNTS = 5;
    private static final int NUM_ITERATIONS = 1000000;
//
//    public void test(){
//        final Random rnd = new Random();
//        final Account[] accounts = new Account[NUM_ACCOUNTS];
//        for (int i = 0; i < accounts.length; i++)
//            accounts[i] = new Account();
//        class TransferThread extends Thread {
//            public void run() {
//                for (int i=0; i<NUM_ITERATIONS; i++) {
//                    int fromAcct = rnd.nextInt(NUM_ACCOUNTS);
//                    int toAcct = rnd.nextInt(NUM_ACCOUNTS);
//                    DollarAmount amount = new DollarAmount(new BigDecimal(rnd.nextInt()));
//                    bankService.transferMoney(accounts[fromAcct], accounts[toAcct], amount);
//                }
//            }
//        }
//        for (int i = 0; i < NUM_THREADS; i++)
//            new TransferThread().start();
//    }

    private static Account initTestAccount(BigDecimal balance) {
        return new Account(new DollarAmount(balance));
    }
}
