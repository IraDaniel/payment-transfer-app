package io.daniel.service.impl;

import io.daniel.dao.AccountDao;
import io.daniel.dao.impl.AccountDaoH2Impl;
import io.daniel.dao.impl.JdbcConnectionHolder;
import io.daniel.model.Account;
import io.daniel.model.CurrencyCode;
import io.daniel.service.AccountService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.daniel.utils.AssertionUtils.notNull;

public class AccountServiceImpl implements AccountService {

    private static AccountDao accountDao = AccountDaoH2Impl.getInstance();
    private static JdbcConnectionHolder connections = JdbcConnectionHolder.getInstance();

    private static volatile AccountServiceImpl instance;

    public static AccountServiceImpl getInstance() {
        if (instance == null) {
            synchronized (AccountService.class) {
                if (instance == null) {
                    instance = new AccountServiceImpl();
                }
            }
        }
        return instance;
    }

    public Account getAccount(Integer accountId) {
        notNull(accountId, "Cannot find account, because id is not defined.");
        return accountDao.getById(accountId);
    }

    public List<Account> getAllAccounts() {
        List<Account> result;
        try {
            connections.beginTransaction();
            result = accountDao.getAll();
            connections.commit();
        } catch (Throwable e) {
            connections.rollBack();
            throw e;
        } finally {
            connections.closeConnection();
        }
        return result;
    }

    public Integer createNewAccount(Account account) {
        Integer accountId;
        try {
            connections.beginTransaction();
            accountId = accountDao.create(account);
            connections.commit();
        } catch (Throwable e) {
            connections.rollBack();
            throw e;
        } finally {
            connections.closeConnection();
        }
        return accountId;
    }

    public void update(List<Account> accounts) {
        if (accounts != null) {
            try {
                connections.beginTransaction();
                Map<Integer, CurrencyCode> accountIdToCurrency = accounts.stream()
                        .collect(Collectors.toMap(Account::getId, Account::getCurrencyCode));
                List<Account> old = accountDao.findByIds(accountIdToCurrency.keySet());
                boolean currencyIsNotModified = old.stream().allMatch(account -> accountIdToCurrency.get(account.getId()) == account.getCurrencyCode());
                if (!currencyIsNotModified) {
                    throw new RuntimeException("Try to change currency");
                }
                accountDao.update(accounts);
                connections.commit();
            } catch (Throwable e) {
                connections.rollBack();
                throw e;
            } finally {
                connections.closeConnection();
            }
        }
    }

    public void delete(Integer accountId) {
        notNull(accountId, "Cannot remove account with undefined id.");
        try {
            connections.beginTransaction();
            accountDao.delete(accountId);
            connections.commit();
        } catch (Throwable e) {
            connections.rollBack();
            throw e;
        } finally {
            connections.closeConnection();
        }
    }

    private AccountServiceImpl() {
    }
}
