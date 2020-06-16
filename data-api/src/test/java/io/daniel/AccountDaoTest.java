package io.daniel;

import io.daniel.dao.AccountDao;
import io.daniel.dao.impl.AccountDaoH2Impl;
import io.daniel.dao.impl.JdbcConnectionHolder;
import io.daniel.exception.EntityNotFoundException;
import io.daniel.model.Account;
import io.daniel.model.DollarAmount;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AccountDaoTest {

    private final AccountDao accountDao = AccountDaoH2Impl.getInstance();

    @Before
    public void beginTransaction() {
        connections.beginTransaction();
    }

    @After
    public void rollbackTransaction() {
        connections.rollBack();
    }

    @Test
    public void getsTheSameAccountAsWasCreated() {
        Random rnd = new Random();
        Account origin = initTestAccount(new BigDecimal(rnd.nextInt()));
        Integer id = accountDao.create(origin);

        Account created = accountDao.getById(id);
        assertEquals(created.getId(), id);
        assertEquals(created.getBalance().getValue(), origin.getBalance().getValue());
    }

    @Test
    public void shouldGetAccountsAsWereCreated() {
        Random rnd = new Random();
        List<Integer> ids = new ArrayList<>();
        ids.add(accountDao.create(initTestAccount(new BigDecimal(rnd.nextInt()))));
        ids.add(accountDao.create(initTestAccount(new BigDecimal(rnd.nextInt()))));
        ids.add(accountDao.create(initTestAccount(new BigDecimal(rnd.nextInt()))));

        List<Account> accountList = accountDao.findByIds(ids);
        Assert.assertNotNull(accountList);
        Assert.assertTrue(accountList.stream().map(Account::getId).allMatch(ids::contains));
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

    @Test(expected = EntityNotFoundException.class)
    public void deletingAccountConfirms_ifAccountIsRemoved() {
        Account origin = initTestAccount(new BigDecimal(new Random().nextInt()));
        Integer id = accountDao.create(origin);
        accountDao.delete(id);

        accountDao.getById(id);
    }

    private static Account initTestAccount(BigDecimal balance) {
        return new Account(new DollarAmount(balance));
    }

    private static JdbcConnectionHolder connections = JdbcConnectionHolder.getInstance();
}
