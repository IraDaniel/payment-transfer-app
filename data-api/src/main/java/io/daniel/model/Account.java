package io.daniel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account implements Serializable {
    public static final String ENTITY_TYPE = "Account";
    private Integer id;
    private Money balance;

    public Account(Money money) {
        this.balance = money;
    }

    public void debit(BigDecimal amount) {
        balance.setValue(balance.getValue().subtract(amount));
    }

    public void credit(BigDecimal amount) {
        balance.setValue(balance.getValue().add(amount));
    }

    public boolean hasEnoughMoney(BigDecimal amount) {
        return balance.getValue().compareTo(amount) >= 0;
    }

    public boolean hasTheSameCurrencyCode(Account another) {
        return balance.getCurrencyCode() == another.getBalance().getCurrencyCode();
    }

    public CurrencyCode getCurrencyCode() {
        return getBalance().getCurrencyCode();
    }
}
