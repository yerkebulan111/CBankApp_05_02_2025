package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionService {

    private final AccountDAO accountDAO;

    public TransactionService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public boolean processDeposit(int accountId, double amount) {
        String updateAccountSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, type, amount, transaction_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Update account balance
            try (PreparedStatement stmt = conn.prepareStatement(updateAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountId);
                stmt.executeUpdate();
            }

            // Insert transaction log
            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, "deposit");
                stmt.setDouble(3, amount);
                stmt.setString(4, "credit");
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.out.println("Error processing deposit: " + e.getMessage());
            return false;
        }
    }

    public boolean processWithdrawal(int accountId, double amount) {
        String updateAccountSql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, type, amount, transaction_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

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

            // Insert transaction log
            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, "withdrawal");
                stmt.setDouble(3, amount);
                stmt.setString(4, "debit");
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.out.println("Error processing withdrawal: " + e.getMessage());
            return false;
        }
    }
}
