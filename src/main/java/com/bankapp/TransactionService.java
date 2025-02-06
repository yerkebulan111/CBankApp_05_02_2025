package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(updateAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, "deposit");
                stmt.setDouble(3, amount);
                stmt.setString(4, "credit");
                stmt.executeUpdate();
            }

            conn.commit();
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
            conn.setAutoCommit(false);

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

            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, "withdrawal");
                stmt.setDouble(3, amount);
                stmt.setString(4, "debit");
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error processing withdrawal: " + e.getMessage());
            return false;
        }
    }

    public boolean transferMoney(int senderAccountId, String receiverUsername, double amount) {
        String getReceiverAccountSql = "SELECT id FROM accounts WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        String updateSenderAccountSql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String updateReceiverAccountSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, type, amount, transaction_type) VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // getReceiverAccountSql:
            int receiverAccountId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(getReceiverAccountSql)) {
                stmt.setString(1, receiverUsername);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    receiverAccountId = rs.getInt("id");
                }
            }

            if (receiverAccountId == -1) {
                System.out.println("Receiver not found.");
                return false;
            }

            // to check if sender have enough money to send &
            // updateSenderAccountSql:
            try (PreparedStatement stmt = conn.prepareStatement(updateSenderAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, senderAccountId);
                stmt.setDouble(3, amount);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Insufficient funds.");
                    return false;
                }
            }

            // updateReceiverAccountSql:
            try (PreparedStatement stmt = conn.prepareStatement(updateReceiverAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, receiverAccountId);
                stmt.executeUpdate();
            }

            // create transaction for sender
            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, senderAccountId);
                stmt.setString(2, "transfer");
                stmt.setDouble(3, amount);
                stmt.setString(4, "debit");
                stmt.executeUpdate();
            }

            // create transaction for receiver
            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, receiverAccountId);
                stmt.setString(2, "transfer");
                stmt.setDouble(3, amount);
                stmt.setString(4, "credit");
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error while transferring money: " + e.getMessage());
            try {
                conn.rollback();  // Rollback if an error occurs
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        }
    }


}
