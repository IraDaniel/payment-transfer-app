package io.daniel.dao.impl;

import io.daniel.dao.AccountDao;
import io.daniel.exception.DaoException;
import io.daniel.exception.EntityNotFoundException;
import io.daniel.model.Account;
import io.daniel.model.CurrencyCode;
import io.daniel.model.Money;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

public class AccountDaoH2Impl implements AccountDao {

    private static final String TABLE_NAME = "ACCOUNT";

    private static final String SELECT_FROM_ACCOUNT = format("SELECT %s FROM %s", String.join(",", Column.ID.name(), Column.BALANCE.name(), Column.CURRENCY_CODE.name()), TABLE_NAME);
    private static final String SELECT_FROM_ACCOUNT_BY_ID = format(SELECT_FROM_ACCOUNT + " where %s = ?", Column.ID);
    private static final String DELETE_FROM_ACCOUNT = format("DELETE FROM %s WHERE %s = ?", TABLE_NAME, Column.ID);
    private static final String UPDATE_ACCOUNT = format("UPDATE %s SET %s =  ? WHERE %s = ?", TABLE_NAME, Column.BALANCE, Column.ID);
    private static final String INSERT_INTO_ACCOUNT = format("insert into %s (%s, %s) values (?,?)", TABLE_NAME, Column.BALANCE, Column.CURRENCY_CODE);

    private static volatile AccountDaoH2Impl instance;

    public static AccountDaoH2Impl getInstance() {
        if (instance == null) {
            synchronized (AccountDaoH2Impl.class) {
                if (instance == null) {
                    instance = new AccountDaoH2Impl();
                }
            }
        }
        return instance;
    }

    private AccountDaoH2Impl() {
        this.connectionHolder = JdbcConnectionHolder.getInstance();
    }

    @Override
    public Integer create(Account account) {
        String sql = INSERT_INTO_ACCOUNT;
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account.getBalance().getValue().toString());
            statement.setString(2, account.getBalance().getCurrencyCode().toString());
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DaoException("An error has occurred during extract created ID.");
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot not create account", e);
        }
    }

    @Override
    public Account update(Account account) {
        String sql = UPDATE_ACCOUNT;
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getBalance().getValue().toString());
            statement.setInt(2, account.getId());
            int updateRows = statement.executeUpdate();
            if (updateRows <= 0) {
                throw new EntityNotFoundException(Account.ENTITY_TYPE, account.getId());
            }
        } catch (SQLException e) {
            throw new DaoException(format("Cannot not update account with id [%s]", account.getId()), e);
        }
        return account;
    }

    @Override
    public void update(List<Account> accountList) {
        String sql = UPDATE_ACCOUNT;
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            accountList.forEach(account -> {
                try {
                    statement.setString(1, account.getBalance().getValue().toString());
                    statement.setInt(2, account.getId());
                    statement.addBatch();
                } catch (SQLException e) {
                    throw new DaoException("Cannot not update accounts", e);
                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DaoException("Cannot not update accounts", e);
        }
    }

    @Override
    public Account getById(Integer accountId) {
        Account account = null;
        String sql = SELECT_FROM_ACCOUNT_BY_ID;
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.first()) {
                    account = getFromResultSet(resultSet);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new DaoException(format("Cannot find account by id: %s", accountId), e);
        }

        if (account == null) {
            throw new EntityNotFoundException(Account.ENTITY_TYPE, accountId);
        }
        return account;
    }

    @Override
    public List<Account> findByIds(Collection<Integer> accountIds) {
        List<Account> accounts = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        accountIds.forEach(id -> builder.append("?,"));

        String sql = SELECT_FROM_ACCOUNT + " where ID in (" + builder.deleteCharAt(builder.length() - 1).toString() + ")";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (Integer id : accountIds) {
                statement.setInt(index, id);
                index++;
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Account account = getFromResultSet(resultSet);
                    accounts.add(account);
                }
            } catch (SQLException e) {
                throw new DaoException("Cannot find accounts by ids.", e);
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot find accounts by ids.", e);
        }
        return accounts;
    }

    @Override
    public List<Account> getAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = SELECT_FROM_ACCOUNT;
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Account account = getFromResultSet(resultSet);
                    accounts.add(account);
                }
            } catch (SQLException e) {
                throw new DaoException("Cannot extract all accounts", e);
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot extract all accounts", e);
        }
        return accounts;
    }

    @Override
    public void delete(Integer id) {
        String sql = DELETE_FROM_ACCOUNT;
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot remove account with ID" + id, e);
        }
    }

    public static Account getFromResultSet(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(Integer.parseInt(resultSet.getString(Column.ID.name())));
        String balanceValue = resultSet.getString(Column.BALANCE.name());
        account.setBalance(new Money(new BigDecimal(balanceValue),
                CurrencyCode.valueOf(resultSet.getString(Column.CURRENCY_CODE.name()))));
        return account;
    }

    private enum Column {
        ID,
        BALANCE,
        CURRENCY_CODE
    }

    private JdbcConnectionHolder connectionHolder;
}

