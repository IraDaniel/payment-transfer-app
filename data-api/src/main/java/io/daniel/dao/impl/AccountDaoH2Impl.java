package io.daniel.dao.impl;

import io.daniel.dao.AccountDao;
import io.daniel.exception.DaoException;
import io.daniel.exception.EntityNotFoundException;
import io.daniel.model.Account;
import io.daniel.model.CurrencyCode;
import io.daniel.model.Money;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AccountDaoH2Impl implements AccountDao {

    private static final String SELECT_FROM_ACCOUNT = "SELECT " + Column.ID + "," + Column.BALANCE + "," + Column.CURRENCY_CODE + " FROM account";
    private static final String SELECT_FROM_ACCOUNT_BY_ID = SELECT_FROM_ACCOUNT + " where id = ?";
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
        String sql = "insert into account (balance, currency_code) values (?,?)";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setBigDecimal(1, account.getBalance().getValue());
            statement.setString(2, account.getBalance().getCurrencyCode().toString());
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DaoException("Could not create account");
            }
        } catch (SQLException e) {
            throw new DaoException("Could not create account", e);
        }
    }

    @Override
    public Account update(Account account) {
        String sql = "update account set balance =  ? where id = ?";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, account.getBalance().getValue());
            statement.setInt(2, account.getId());
            int updateRows = statement.executeUpdate();
            if (updateRows <= 0) {
                throw new EntityNotFoundException(Account.ENTITY_TYPE, account.getId());
            }
        } catch (SQLException e) {
            throw new DaoException(String.format("Could not update account with id [%s]", account.getId()), e);
        }
        return account;
    }

    @Override
    public void update(List<Account> accountList) {
        String sql = "update account set balance =  ? where id = ?";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            accountList.forEach(account -> {
                try {
                    statement.setBigDecimal(1, account.getBalance().getValue());
                    statement.setInt(2, account.getId());
                    statement.addBatch();
                } catch (SQLException e) {
                    throw new DaoException("Could not update accounts", e);
                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DaoException("Could not update accounts", e);
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
            throw new DaoException(String.format("Cannot find account by id: %s", accountId), e);
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
                throw new DaoException("Cannot extract all accounts", e);
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot extract all accounts", e);
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

    public static Account getFromResultSet(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(Integer.parseInt(resultSet.getString(Column.ID.name())));
        account.setBalance(new Money(resultSet.getBigDecimal(Column.BALANCE.name()),
                CurrencyCode.valueOf(resultSet.getString(Column.CURRENCY_CODE.name()))));
        return account;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM account WHERE id = ?";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot remove account with ID" + id, e);
        }
    }

    private enum Column {
        ID,
        BALANCE,
        CURRENCY_CODE
    }

    private JdbcConnectionHolder connectionHolder;
}

