package io.daniel;

import io.daniel.dao.AccountDao;
import io.daniel.dao.impl.AccountDaoH2Impl;
import io.daniel.model.Account;
import io.daniel.model.DollarAmount;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AccountDaoTest {

    private final AccountDao accountDao = AccountDaoH2Impl.getInstance();

    @Test
    public void getsTheSameAccountAsWasCreated() {
        Random rnd = new Random();
        Account origin = initTestAccount(new BigDecimal(rnd.nextInt()));
        Integer id = accountDao.create(origin);

        Account created = accountDao.getById(id);
        assertEquals(created.getId(), id);
        assertTrue(created.getBalance().getValue().compareTo(origin.getBalance().getValue()) == 0);
    }

    @Test
    public void shouldGetAllAccountsAsWereCreated() {
        Random rnd = new Random();
        Integer id1 = accountDao.create(initTestAccount(new BigDecimal(rnd.nextInt())));
        Integer id2 = accountDao.create(initTestAccount(new BigDecimal(rnd.nextInt())));
        Integer id3 = accountDao.create(initTestAccount(new BigDecimal(rnd.nextInt())));

        List<Account> accountList = accountDao.findByIds(Arrays.asList(id1, id2, id3));
        Assert.assertNotNull(accountList);
    }

    @Test
    public void shouldGetAccountAsWasUpdated() {
        Account origin = initTestAccount(new BigDecimal(new Random().nextInt()));
        Integer id = accountDao.create(origin);
        origin = accountDao.getById(id);
        origin.setBalance(new DollarAmount(new BigDecimal(100)));
        accountDao.update(origin);

        Account updated = accountDao.getById(origin.getId());
        assertEquals(updated.getId(), id);
        assertTrue(updated.getBalance().getValue().compareTo(origin.getBalance().getValue()) == 0);
    }

    private static Account initTestAccount(BigDecimal balance) {
        return new Account(new DollarAmount(balance));
    }

}
