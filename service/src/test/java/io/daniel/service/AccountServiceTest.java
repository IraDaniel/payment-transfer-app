package io.daniel.service;

import io.daniel.TestUtils;
import io.daniel.exception.BusinessException;
import io.daniel.model.Account;
import io.daniel.model.CurrencyCode;
import io.daniel.model.Money;
import io.daniel.service.impl.AccountServiceImpl;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Random;

import static io.daniel.TestUtils.initTestAccount;

/**
 * Trying to use PowerMock to mock singleton class: AccountDaoH2Impl,but could not resolve the errors.
 */
public class AccountServiceTest {

    private static AccountService accountService = AccountServiceImpl.getInstance();

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfFindAccountWithNotSpecifyID() {
        accountService.getAccount(null);
    }

    @Test
    public void getsAccountAsWasCreated() {
        Account origin = initTestAccount(new Random().nextInt());
        Integer accountId = accountService.createNewAccount(origin);
        Account created = accountService.getAccount(accountId);

        TestUtils.assertMoney(origin.getBalance(), created.getBalance());
    }

    @Test(expected = BusinessException.class)
    public void shouldThrowExceptionIfTryToUpdateCurrencyCode() {
        Random rnd = new Random();

        Integer accountId = accountService.createNewAccount(initTestAccount(rnd.nextInt()));
        accountService.createNewAccount(initTestAccount(rnd.nextInt()));

        Account account = accountService.getAccount(accountId);
        account.setBalance(new Money(new BigDecimal(rnd.nextInt()), CurrencyCode.RUB));

        accountService.update(Collections.singletonList(account));
    }
}
