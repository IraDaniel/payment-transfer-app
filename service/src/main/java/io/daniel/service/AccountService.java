package io.daniel.service;

import io.daniel.dao.AccountDao;
import io.daniel.dao.AccountDaoLocalImpl;
import io.daniel.model.Account;

import java.math.BigDecimal;
import java.util.List;


public class AccountService {
    private static AccountDao accountDao = AccountDaoLocalImpl.getInstance();

    private static AccountService instance;

    private AccountService() {
    }

    public static AccountService getInstance() {
        if (instance == null) {
            synchronized (AccountService.class) {
                if (instance == null) {
                    instance = new AccountService();
                }
            }
        }
        return instance;
    }

    public Account getAccount(Integer accountId) {
        BankService.notNull(accountId, "Cannot find account, because id is not defined.");
        return accountDao.getById(accountId);
    }

    public List<Account> getAllAccounts() {
        return accountDao.getAll();
    }

    public Integer createNewAccount(BigDecimal amount) {
        Account account = new Account(amount);
        return accountDao.create(account);
    }

    public void update(List<Account> accounts) {
        if (accounts != null) {
            accountDao.update(accounts);
        }
    }

    public void delete(Integer id) {
        accountDao.delete(id);
    }

}
