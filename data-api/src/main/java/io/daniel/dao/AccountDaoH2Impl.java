package io.daniel.dao;

import io.daniel.exception.DaoException;
import io.daniel.exception.EntityNotFoundException;
import io.daniel.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Ira on 21.11.2019.
 */
public class AccountDaoH2Impl implements AccountDao {

    private enum Column {
        ID,
        BALANCE
    }

    private static int nextId = 0;
    private JdbcConnectionHolder connectionHolder;

    public AccountDaoH2Impl(JdbcConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;

    }

    @Override
    public Integer create(Account account) {
        String sql = "insert into account (id, balance) values ( ?, ?)";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            account.setId(getNextId());
            statement.setInt(1, account.getId());
            statement.setBigDecimal(2, account.getBalance());
            statement.executeQuery();
            return account.getId();
        } catch (SQLException e) {
            throw new DaoException("Could not create account", e);
        }
    }

    @Override
    public Account update(Account account) {
        String sql = "update account set balance =  ? where id = ?";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, account.getBalance());
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
                    statement.setBigDecimal(1, account.getBalance());
                    statement.setInt(2, account.getId());
                    statement.addBatch();
                } catch (SQLException e) {

                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
        }
    }

    @Override
    public Account getById(Integer accountId) {
        Account account = null;
        String sql = "SELECT id, balance FROM account where id = ?";
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

    public int getNextId() {
        String sql = "select count(*) from account";
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            return ++nextId;
        } catch (SQLException e) {
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Account> getAll() {
        return null;
    }

    public static Account getFromResultSet(ResultSet resultSet) throws SQLException{
        Account account = new Account();
        account.setId(Integer.parseInt(resultSet.getString(Column.ID.name())));
        account.setBalance(resultSet.getBigDecimal(Column.BALANCE.name()));
        return account;
    }

    @Override
    public void delete(Integer id) {

    }
}

