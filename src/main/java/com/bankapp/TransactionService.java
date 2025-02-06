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
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return accountDAO.insertTransaction(accountId, "deposit", amount, "credit");
            }
        } catch (SQLException e) {
            System.err.println("Error processing deposit: " + e.getMessage());
        }
        return false;
    }

    public boolean processWithdrawal(int accountId, double amount) {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return accountDAO.insertTransaction(accountId, "withdrawal", amount, "debit");
            }
        } catch (SQLException e) {
            System.err.println("Error processing withdrawal: " + e.getMessage());
        }
        return false;
    }

    public boolean transferMoney(int senderAccountId, String recipientUsername, double amount) {
        try {
            UserDAO userDAO = new UserDAO();
            int recipientUserId = userDAO.getUserId(recipientUsername);
            if (recipientUserId == -1) {
                System.out.println("Recipient not found.");
                return false;
            }

            int recipientAccountId = accountDAO.getAccountIdByUserId(recipientUserId);
            if (recipientAccountId == -1) {
                System.out.println("Recipient does not have an account.");
                return false;
            }

            if (accountDAO.checkBalance(senderAccountId) < amount) {
                System.out.println("Insufficient funds.");
                return false;
            }

            if (accountDAO.transferMoney(senderAccountId, recipientAccountId, amount)) {
                try {
                    accountDAO.insertTransaction(senderAccountId, "transfer", amount, "debit");
                    accountDAO.insertTransaction(recipientAccountId, "transfer", amount, "credit");
                } catch (SQLException e) {
                    System.err.println("Error logging transaction: " + e.getMessage());
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error processing transfer: " + e.getMessage());
        }
        return false;
    }
}