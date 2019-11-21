package io.daniel.service;

import io.daniel.dao.AccountDao;
import io.daniel.dao.AccountDaoLocalImpl;
import io.daniel.exception.InsufficientFundsException;
import io.daniel.model.Account;

import java.math.BigDecimal;

/**
 * Created by Ira on 21.11.2019.
 */
public class BankServiceSecondImpl implements BankService {

    private final AccountDao accountDao = AccountDaoLocalImpl.getInstance();

    @Override
    public void transferMoney(Integer fromAcctId, Integer toAcctId, BigDecimal amount) throws InterruptedException {
        class Helper {
            public void transfer() throws InsufficientFundsException {
                Account fromAcct = accountDao.getById(fromAcctId);
                Account toAcct = accountDao.getById(toAcctId);
                if (fromAcct.getBalance().compareTo(amount) < 0)
                    throw new InsufficientFundsException("Not enough funds in the account " + fromAcctId);
                else {
                    fromAcct.debit(amount);
                    toAcct.credit(amount);
                }
            }
        }

        if (fromAcctId < toAcctId) {
            synchronized (fromAcctId) {
                synchronized (toAcctId) {
                    new Helper().transfer();
                }
            }
        } else {
            synchronized (toAcctId) {
                synchronized (fromAcctId) {
                    new Helper().transfer();
                }
            }
        }
    }
}
