package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AccountFactory {
    public static boolean createAccount(int userId) {
        String sql = "INSERT INTO accounts (user_id, balance, account_type) VALUES (?, 0, 'debit')";
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
}
