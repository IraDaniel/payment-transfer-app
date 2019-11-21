package io.daniel.dao;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Ira on 21.11.2019.
 */
public class JdbcConnectionHolder {
    private ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
    private JdbcDataSource dataSource;

    public JdbcConnectionHolder() {
        dataSource = initializeDataSource("sa", "", "jdbc:h2:~/test;DB_CLOSE_DELAY=-1;");
    }

    public static JdbcDataSource initializeDataSource(String username, String password, String url) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    public Connection getConnection() {
        Connection connection = threadLocalConnection.get();
        if (connection == null) {
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                threadLocalConnection.set(connection);
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        }
        return connection;
    }

    public void closeConnection() {
        Connection connection = threadLocalConnection.get();
        try {
            if (connection != null) {
                connection.close();
                threadLocalConnection.remove();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        Connection connection = getConnection();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error during commit");
        }
    }

    public void rollBack() {
        Connection connection = getConnection();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Error during rollback");
        }
    }
}
