package com.bankapp;

import java.sql.*;

public class AccountDAO {

    public int getAccountIdByUserId(int userId) {
        String sql = "SELECT account_id FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("account_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account ID: " + e.getMessage());
        }
        return -1;
    }

    public boolean createAccount(int userId) {
        String sql = "INSERT INTO accounts (user_id, balance) VALUES (?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        }
        return false;
    }

    public double checkBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking balance: " + e.getMessage());
        }
        return 0;
    }

    public boolean transferMoney(int senderAccountId, int recipientAccountId, double amount) {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, senderAccountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {

                sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                    stmt2.setDouble(1, amount);
                    stmt2.setInt(2, recipientAccountId);
                    int rowsAffected2 = stmt2.executeUpdate();
                    return rowsAffected2 > 0;
                } catch (SQLException e) {
                    System.err.println("Error transfer money: " + e.getMessage());
                    conn.rollback();
                }
            }
            conn.rollback();

        } catch (SQLException e) {
            System.err.println("Error transfer money: " + e.getMessage());
        }
        return false;
    }

    public boolean applyForLoan(int userId, double amount, int interestRate, int term) {
        String sql = "INSERT INTO loans (user_id, amount, interest_rate, term) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.setInt(3, interestRate);
            stmt.setInt(4, term);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error applying for loan: " + e.getMessage());
        }
        return false;
    }

    public boolean insertTransaction(int accountId, String type, double amount, String category) {
        String sql = "INSERT INTO transactions (account_id, type, amount, category, transaction_date) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setString(4, category);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting transaction: " + e.getMessage());
        }
        return false;
    }
}