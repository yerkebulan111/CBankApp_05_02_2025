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
            System.out.println("Error processing depositing money: " + e.getMessage());
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
                stmt.setString(2, "transfer");
                stmt.setDouble(3, amount);
                stmt.setString(4, "debit");
                stmt.executeUpdate();
            }

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
    public void showTransactionHistory(int accountId) {
        String sql = "SELECT t.account_id, t.type, t.amount, t.transaction_type, t.created_at, u.username " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.id " +
                "JOIN users u ON a.user_id = u.id " +
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
                String username = rs.getString("username");

                if (transactionType.equals("credit")) {
                    System.out.println("From: " + username + " - To: You - Amount: " + amount + "$"+ " - Date: " + transactionDate);
                } else if (transactionType.equals("debit")) {
                    System.out.println("From: You - To: " + username + " - Amount: " + amount + "$" +" - Date: " + transactionDate);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching transaction history: " + e.getMessage());
        }
    }


}
//отправить на депозит - добавить счетчик логинов - логин +1 депозит - упал процент за месяц