package com.disasterrelief.utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBConnection provides resilient JDBC connection pooling and environment-aware configuration.
 * Adheres to academic assignment specifications while supporting local Windows and OCI production environments.
 */
public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    private static volatile HikariDataSource dataSourceInstance;
    private static final Object lock = new Object();

    private DBConnection() {
        // Utility / Singleton class
    }

    /**
     * Obtains the pooled DataSource instance configured from environment variables
     * or fallback system properties.
     */
    public static DataSource getDataSource() {
        if (dataSourceInstance == null) {
            synchronized (lock) {
                if (dataSourceInstance == null) {
                    dataSourceInstance = initializeDataSource();
                }
            }
        }
        return dataSourceInstance;
    }

    private static HikariDataSource initializeDataSource() {
        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "3307");
        String dbName = getEnvOrDefault("DB_NAME", "disaster_relief_db");
        String user = getEnvOrDefault("DB_USER", "host");
        String password = getEnvOrDefault("DB_PASSWORD", "1234");

        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                host, port, dbName
        );

        logger.info("Initializing DBConnection pool to host [{}:{}] for database [{}] as user [{}]",
                host, port, dbName, user);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setPoolName("DREMLDS-HikariPool");

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }

    /**
     * Acquires a database connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Performs a health check on the database connection pool.
     * @return true if database responds within timeout, false otherwise.
     */
    public static boolean checkConnectionHealth() {
        try (Connection conn = getConnection()) {
            return conn.isValid(3);
        } catch (Exception e) {
            logger.warn("Database connection health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Closes the active HikariDataSource connection pool.
     */
    public static void closePool() {
        synchronized (lock) {
            if (dataSourceInstance != null && !dataSourceInstance.isClosed()) {
                logger.info("Closing DBConnection Hikari pool");
                dataSourceInstance.close();
                dataSourceInstance = null;
            }
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(key, defaultValue);
        }
        return value != null ? value.trim() : defaultValue;
    }
}

