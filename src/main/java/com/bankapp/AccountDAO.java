package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {



    public int getAccountIdByUserId(int userId) {
        String sql = "SELECT id FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching account ID: " + e.getMessage());
        }
        return -1;
    }

    public double checkBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.out.println("Error while checking balance: " + e.getMessage());
        }
        return -1;
    }

    public boolean deposit(int accountId, double amount) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error while depositing money: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(int accountId, double amount) {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            stmt.setDouble(3, amount);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error while withdrawing money: " + e.getMessage());
            return false;
        }
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

    public String getFullAccountDescription(int accountId) {
        String sql = "SELECT a.id AS accountId, a.balance, a.account_type, " +
                "u.id AS userId, u.name, u.email " +
                "FROM accounts a " +
                "JOIN users u ON a.user_id = u.id " +
                "WHERE a.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int accId = rs.getInt("accountId");
                double balance = rs.getDouble("balance");
                String accountType = rs.getString("account_type");
                int userId = rs.getInt("userId");
                String name = rs.getString("name");
                String email = rs.getString("email");
                return "Account ID: " + accId +
                        ", Balance: " + balance +
                        ", Account Type: " + accountType +
                        ", User ID: " + userId +
                        ", Name: " + name +
                        ", Email: " + email;
            } else {
                return "Account not found.";
            }
        } catch (SQLException e) {
            return "Error while fetching full account description: " + e.getMessage();
        }
    }
}
