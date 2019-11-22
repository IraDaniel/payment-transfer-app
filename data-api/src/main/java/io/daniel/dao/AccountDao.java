package io.daniel.dao;

import io.daniel.model.Account;

import java.util.Collection;
import java.util.List;

public interface AccountDao {

    Integer create(Account account);

    void delete(Integer id);

    Account update(Account account);

    void update(List<Account> accountList);

    Account getById(Integer accountId);

    List<Account> findByIds(Collection<Integer> accountIds);

    List<Account> getAll();
}