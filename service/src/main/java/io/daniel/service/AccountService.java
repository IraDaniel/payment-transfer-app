package io.daniel.service;

import io.daniel.model.Account;

import java.util.List;


public interface AccountService {

    Account getAccount(Integer accountId);

    List<Account> getAllAccounts();

    Integer createNewAccount(Account account);

    void update(List<Account> accounts);

    void delete(Integer accountId);
}
