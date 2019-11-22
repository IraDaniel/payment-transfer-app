package io.daniel.service;

import io.daniel.model.Money;


public interface BankService {

    void transferMoney(final Integer fromAcctId,
                       final Integer toAcctId,
                       final Money amount) throws InterruptedException;
}
