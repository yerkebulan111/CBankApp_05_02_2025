package com.bankapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final DatabaseConnection instance = new DatabaseConnection();
    private static final String URL = "jdbc:postgresql://localhost:5432/bank_app";
    private static final String USER = "postgres";
    private static final String PASSWORD = "0000";

    private DatabaseConnection() { }

    private Connection createConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static Connection getConnection() {
        return instance.createConnection();
    }
}
