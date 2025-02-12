package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private AccountDAO accountDAO = new AccountDAO();

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            String getUserIdSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(getUserIdSql)) {
                userStmt.setString(1, username);
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    AccountFactory.createAccount(userId);                }
            }
        } catch (SQLException e) {
            System.out.println("Error while registering user: " + e.getMessage());
            return false;
        }
        return false;
    }

    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    public int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching user ID: " + e.getMessage());
        }
        return -1;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error while deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBalance(int userId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
            return false;
        }
    }

    public double getBalance(int userId) {
        String sql = "SELECT balance FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching balance: " + e.getMessage());
        }
        return -1;
    }

    public String getFullUserDescription(int userId) {
        String sql = "SELECT u.id AS userId, u.username, a.id AS accountId, a.balance, a.account_type " +
                "FROM users u JOIN accounts a ON u.id = a.user_id WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int uid = rs.getInt("userId");
                String username = rs.getString("username");
                int accountId = rs.getInt("accountId");
                double balance = rs.getDouble("balance");
                String accountType = rs.getString("account_type");
                return "User ID: " + uid + ", Username: " + username + ", Account ID: " + accountId +
                        ", Balance: " + balance + ", Account Type: " + accountType;
            } else {
                return "User not found.";
            }
        } catch (SQLException e) {
            return "Error retrieving user details: " + e.getMessage();
        }
    }
}
