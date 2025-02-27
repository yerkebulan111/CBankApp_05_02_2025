package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DepositAccountDAO {

    public boolean openDepositAccount(int userId, double initialAmount) {
        String sql = "INSERT INTO deposit_accounts (user_id, deposit_balance, login_count) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, initialAmount);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error in opening a deposit account: " + e.getMessage());
            return false;
        }
    }

    public boolean depositToDepositAccount(int userId, double amount) {
        String sql = "UPDATE deposit_accounts SET deposit_balance = deposit_balance + ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Deposit account replenishment error: " + e.getMessage());
            return false;
        }
    }

    public boolean depositAccountExists(int userId) {
        String sql = "SELECT id FROM deposit_accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Deposit account verification error: " + e.getMessage());
            return false;
        }
    }

    public double getDepositBalance(int userId) {
        String sql = "SELECT deposit_balance FROM deposit_accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("deposit_balance");
            }
        } catch (SQLException e) {
            System.out.println("error in obtaining the deposit account balance: " + e.getMessage());
        }
        return 0;
    }

    public void incrementLoginCountAndApplyBonus(int userId) {
        String getSql = "SELECT deposit_balance, login_count FROM deposit_accounts WHERE user_id = ?";
        String updateSql = "UPDATE deposit_accounts SET deposit_balance = ?, login_count = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(getSql)) {
            getStmt.setInt(1, userId);
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("deposit_balance");
                int loginCount = rs.getInt("login_count");
                loginCount++;
                if (balance > 1000) {
                    balance += balance * 0.01;
                }
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, balance);
                    updateStmt.setInt(2, loginCount);
                    updateStmt.setInt(3, userId);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating the deposit account when logging in: " + e.getMessage());
        }
    }

    public void updateDepositBalance(int userId, double newBalance) {
        String sql = "UPDATE deposit_accounts SET deposit_balance = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getFullDepositAccountDescription(int userId) {
        String sql = "SELECT d.id AS depositAccountId, d.deposit_balance, d.login_count, u.id AS userId, u.username, u.email " +
                "FROM deposit_accounts d JOIN users u ON d.user_id = u.id WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int depositAccountId = rs.getInt("depositAccountId");
                double depositBalance = rs.getDouble("deposit_balance");
                int loginCount = rs.getInt("login_count");
                String username = rs.getString("username");
                String email = rs.getString("email");
                return "Deposit Account ID: " + depositAccountId +
                        ", Deposit Balance: " + depositBalance +
                        ", Login Count: " + loginCount +
                        ", User ID: " + userId +
                        ", Username: " + username +
                        ", Email: " + email;
            } else {
                return "Deposit account not found for user id " + userId;
            }
        } catch (SQLException e) {
            return "Error retrieving deposit account details: " + e.getMessage();
        }
    }
}
