package io.daniel.service;

import java.math.BigDecimal;


public interface BankService {

    void transferMoney(final Integer fromAcctId,
                       final Integer toAcctId,
                       final BigDecimal amount) throws InterruptedException;
}
