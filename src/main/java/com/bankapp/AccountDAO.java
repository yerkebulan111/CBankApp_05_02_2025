package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

    public boolean createAccount(int userId) {
        String sql = "INSERT INTO accounts (user_id, balance) VALUES (?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error while creating account: " + e.getMessage());
            return false;
        }
    }

    public boolean deposit(int accountId, double amount) {
        String updateAccountSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, type, amount, transaction_type) VALUES (?, ?, ?, ?)";

        Connection conn = null;  // Declare 'conn' outside the try block
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Update account balance
            try (PreparedStatement stmt = conn.prepareStatement(updateAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountId);
                stmt.executeUpdate();
            }

            // Log the transaction
            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, "deposit");
                stmt.setDouble(3, amount);
                stmt.setString(4, "credit"); // assuming it's a credit transaction
                stmt.executeUpdate();
            }

            conn.commit(); // Commit the transaction
            return true;

        } catch (SQLException e) {
            System.out.println("Error while depositing money: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback if any error occurs
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close(); // Ensure the connection is closed after use
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public boolean withdraw(int accountId, double amount) {
        String updateAccountSql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, type, amount, transaction_type) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Update account balance
            try (PreparedStatement stmt = conn.prepareStatement(updateAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountId);
                stmt.setDouble(3, amount);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Insufficient funds.");
                    return false;
                }
            }

            // Log the transaction
            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, "withdrawal");
                stmt.setDouble(3, amount);
                stmt.setString(4, "debit"); // assuming it's a debit transaction
                stmt.executeUpdate();
            }

            conn.commit(); // Commit the transaction
            return true;

        } catch (SQLException e) {
            System.out.println("Error while withdrawing money: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback if any error occurs
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close(); // Ensure the connection is closed after use
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public double checkBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {  // Now 'rs' is recognized as ResultSet
                return rs.getDouble("balance");
            }

        } catch (SQLException e) {
            System.out.println("Error while checking balance: " + e.getMessage());
        }
        return -1;
    }

    public boolean deleteAccount(int accountId) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error while deleting account: " + e.getMessage());
            return false;
        }
    }
}
