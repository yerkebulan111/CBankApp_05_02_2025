package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;



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
            System.out.println("Error processing depositing money: " + e.getMessage());
            return false;
        }
    }

    public boolean transferMoney(int senderAccountId, String receiverUsername, double amount) {
        String getReceiverAccountSql = "SELECT id FROM accounts WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        String updateSenderAccountSql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String updateReceiverAccountSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, receiver_account_id, type, amount, transaction_type, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

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

            try (PreparedStatement stmt = conn.prepareStatement(updateReceiverAccountSql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, receiverAccountId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, senderAccountId);
                stmt.setInt(2, receiverAccountId);
                stmt.setString(3, "transfer");
                stmt.setDouble(4, amount);
                stmt.setString(5, "debit");
                stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertTransactionSql)) {
                stmt.setInt(1, receiverAccountId);
                stmt.setInt(2, senderAccountId);
                stmt.setString(3, "transfer");
                stmt.setDouble(4, amount);
                stmt.setString(5, "credit");
                stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error while transferring money: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public void showTransactionHistory(int accountId) {
        String sql = "SELECT t.account_id, t.receiver_account_id, t.type, t.amount, t.transaction_type, t.created_at, u1.username AS sender_username, u2.username AS receiver_username " +
                "FROM transactions t " +
                "JOIN accounts a1 ON t.account_id = a1.id " +
                "JOIN users u1 ON a1.user_id = u1.id " +
                "LEFT JOIN accounts a2 ON t.receiver_account_id = a2.id " +
                "LEFT JOIN users u2 ON a2.user_id = u2.id " +
                "WHERE t.account_id = ? OR t.receiver_account_id = ? " +
                "ORDER BY t.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("--- Transaction History ---");
            while (rs.next()) {
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                String transactionType = rs.getString("transaction_type");
                String transactionDate = rs.getString("created_at");
                String senderUsername = rs.getString("sender_username");
                String receiverUsername = rs.getString("receiver_username");

                if (transactionType.equals("credit")) {
                    System.out.println("From: " + senderUsername + " - To: You - Amount: " + amount + "$" + " - Date: " + transactionDate);
                }
                else  if (transactionType.equals("debit")) {
                    System.out.println("From: You - To: " + receiverUsername + " - Amount: " + amount + "$" + " - Date: " + transactionDate);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching transaction history: " + e.getMessage());
        }
    }

}
