package io.daniel.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class JdbcConnectionHolder {

    private static final String DB_USERNAME = "db.url";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_URL = "db.url";

    private static volatile JdbcConnectionHolder instance;
    public static JdbcConnectionHolder getInstance() {
        if (instance == null) {
            synchronized (JdbcConnectionHolder.class) {
                if (instance == null) {
                    instance = new JdbcConnectionHolder();
                }
            }
        }
        return instance;
    }
    private JdbcConnectionHolder() {
        Properties prop = readProperties();
        if (prop != null) {
            dataSource = initializeDataSource(prop.getProperty(DB_USERNAME), prop.getProperty(DB_PASSWORD), prop.getProperty(DB_URL));
            Flyway flyway = Flyway.configure().dataSource(dataSource).locations("sql").load();
            flyway.migrate();
        }
    }

    private Properties readProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("h2.database.connection.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load property file with database properties.", ex);
        }
    }

    private static JdbcDataSource initializeDataSource(String username, String password, String url) {
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

    public Connection beginTransaction() {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            closeConnection();
            throw new RuntimeException(e);
        }
        return connection;
    }

    private ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
    private JdbcDataSource dataSource;
}
