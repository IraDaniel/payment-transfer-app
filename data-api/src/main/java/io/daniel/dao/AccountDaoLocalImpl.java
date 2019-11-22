package io.daniel.dao;

import io.daniel.exception.EntityNotFoundException;
import io.daniel.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountDaoLocalImpl implements AccountDao {

    private static AccountDaoLocalImpl instance;
    private Map<Integer, Account> accountMap = new ConcurrentHashMap<>();
    private static int nextId = 0;

    private AccountDaoLocalImpl() {
    }

    public static AccountDaoLocalImpl getInstance() {
        if (instance == null) {
            synchronized (AccountDaoLocalImpl.class) {
                if (instance == null) {
                    instance = new AccountDaoLocalImpl();
                }
            }
        }
        return instance;
    }


    public Account getById(Integer id) {
        Account account = accountMap.get(id);
        if (account == null) {
            throw new EntityNotFoundException(Account.ENTITY_TYPE, id);
        }
        return account;
    }

    public Integer create(Account account) {
        if (account.getId() == null) {
            account.setId(createNewId());
        }
        accountMap.put(account.getId(), account);
        return account.getId();
    }

    public Account update(Account account) {
        if (null == accountMap.replace(account.getId(), account)) {
            return null;
        }
        // Update succeeded return the user
        return account;
    }

    public void update(List<Account> accountList) {
        accountList.forEach(account -> {
            accountMap.replace(account.getId(), account);
        });
    }

    @Override
    public void delete(Integer id) {
        accountMap.remove(id);
    }

    @Override
    public List<Account> getAll() {
        return new ArrayList<>(accountMap.values());
    }

    private static Integer createNewId() {
        return nextId++;
    }
}
