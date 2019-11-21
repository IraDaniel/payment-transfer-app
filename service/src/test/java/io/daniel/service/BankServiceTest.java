package io.daniel.service;

import io.daniel.dao.AccountDao;
import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.when;

/**
 * Created by Ira on 21.11.2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class BankServiceTest {

    private final BankService bankService = BankService.getInstance();

    @InjectMocks
    private final AccountService accountService = AccountService.getInstance();

    @Mock
    private AccountDao accountDao;


    @Test
    public void shouldTransferMoney() throws InterruptedException {
        Account from = initTestAccount(new BigDecimal(123));
        Account to = initTestAccount(new BigDecimal(123));
        when(accountDao.create(from)).thenReturn(0);
        when(accountDao.create(to)).thenReturn(1);
        from.setId(0);
        to.setId(1);
        bankService.transfer(from, to, new BigDecimal(23));

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
        bankService.transfer(from, to, new BigDecimal(26));
    }

    @Test
    public void shouldTransferMoneyInParallel() throws Exception {
        Account from = initTestAccount(new BigDecimal(26));
        Account to = initTestAccount(new BigDecimal(0));
        when(accountDao.create(from)).thenReturn(0);
        when(accountDao.create(to)).thenReturn(1);
        from.setId(0);
        to.setId(1);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.execute(new TestThread(bankService, new TransferTest(from, to, new BigDecimal(26))));
        executor.execute(new TestThread(bankService, new TransferTest(to, from, new BigDecimal(26))));

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }


    private static Account initTestAccount(BigDecimal balance) {
        return new Account(balance);
    }
}
