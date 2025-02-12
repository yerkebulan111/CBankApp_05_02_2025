package com.bankapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BankApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionService transactionService = new TransactionService(accountDAO);
    private static final DepositAccountDAO depositAccountDAO = new DepositAccountDAO();
    private static String userRole;

    private static int loggedInUserId = -1;
    private static int userAccountId = -1;

    public static void main(String[] args) {
        while (true) {
            if (loggedInUserId == -1) {
                showMainMenu();
            } else {
                showAccountMenu();
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("--- Main Menu ---");
        System.out.println("1. Register User");
        System.out.println("2. Login User");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        Runnable action = (choice == 1) ? BankApp::registerUser :
                (choice == 2) ? BankApp::loginUser :
                        () -> System.out.println("Invalid choice.");
        action.run();
    }

    private static void showAccountMenu() {
        // Check if user is a client, manager, or admin and display relevant menu
        if (userRole.equals("client")) {
            // Client menu
            System.out.println("--- Client Account Management ---");
            System.out.println("1. Get the Money");
            System.out.println("2. Withdraw Money to Deposit");
            System.out.println("3. Check Balance");
            System.out.println("4. Transfer Money");
            System.out.println("5. Logout");
            System.out.println("6. Check Deposit Account Balance"); // Added
            System.out.println("7. Transfer from Deposit to Main Account"); // Added
            System.out.println("8. Show Transaction History");

            // Handle Client's menu choice
            handleClientMenu();
        } else if (userRole.equals("manager")) {
            // Manager menu
            System.out.println("--- Manager Account Management ---");
            System.out.println("1. View All Users");
            System.out.println("2. Login to Client Account");
            System.out.println("3. Edit Client Information");
            System.out.println("4. Logout");

            // Handle Manager's menu choice
            handleManagerMenu();
        } else if (userRole.equals("admin")) {
            System.out.println("--- Admin Management ---");
            System.out.println("1. View All Users");
            System.out.println("2. Login to Client Account");
            System.out.println("3. Edit Client Information");
            System.out.println("4. Delete User");
            System.out.println("5. Logout");

            // Handle Admin's menu choice
            handleAdminMenu();
        } else {
            System.out.println("Invalid role! Please contact support.");
        }
    }

    // Handle Client Menu
    private static void handleClientMenu() {
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        switch (choice) {
            case 1:
                depositMoney(); // Existing method for client
                break;
            case 2:
                withdrawMoney(); // Existing method for client
                break;
            case 3:
                checkBalance(); // Existing method for client
                break;
            case 4:
                transferMoney(); // Existing method for client
                break;
            case 5:
                logout(); // Existing method for logout
                break;
            case 6:
                checkDepositBalance(); // Existing method
                break;
            case 7:
                transferFromDeposit(); // Existing method
                break;
            case 8:
                showTransactionHistory(); // Existing method
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    // Handle Manager Menu
    private static void handleManagerMenu() {
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        switch (choice) {
            case 1:
                viewAllUsers(); // Implement this method
                break;
            case 2:
                loginToClientAccount(); // Implement this method
                break;
            case 3:
                editClientInformation(); // Implement this method
                break;
            case 4:
                logout(); // Existing method for logout
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    // Handle Admin Menu (if applicable)
    private static void handleAdminMenu() {
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        switch (choice) {
            case 1:
                viewAllUsers(); // function for viewing all users
                break;
            case 2:
                loginToClientAccount(); // function for logging into client account
                break;
            case 3:
                editClientInformation(); // function for editing client information
                break;
            case 4:
                deleteUser(); // function for deleting a user
                break;
            case 5:
                logout();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }



    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter role (admin/manager/client): ");
        String role = scanner.nextLine();  // New line for the role input

        // Now call registerUser with username, password, and role
        System.out.println(userDAO.registerUser(username, password, role) ? "User registered successfully!" : "Registration failed.");
    }


    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (userDAO.loginUser(username, password)) {
            System.out.println("Login successful! Welcome, " + username + "!");
            loggedInUserId = userDAO.getUserId(username);
            userAccountId = accountDAO.getAccountIdByUserId(loggedInUserId);

            // Fetch the user role and assign it
            userRole = userDAO.getUserRole(username);  // Set userRole
            System.out.println("You are logged in as: " + userRole);  // Optional: print role for debugging
        } else {
            System.out.println("Invalid username or password.");
        }
    }



    private static void depositMoney() {
        executeAmountInput(amount -> {
            System.out.println(transactionService.processDeposit(userAccountId, amount) ? "Deposit successful!" : "Deposit failed.");
        });
    }

    private static void withdrawMoney() {
        executeAmountInput(amount -> {
            double mainBalance = accountDAO.checkBalance(userAccountId);
            if (mainBalance < amount) {
                System.out.println("Insufficient funds.");
            } else if (accountDAO.withdraw(userAccountId, amount)) {
                System.out.println("Withdrawal successful.");
            } else {
                System.out.println("Withdrawal failed.");
            }
        });
    }

    private static void executeAmountInput(java.util.function.Consumer<Double> action) {
        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        action.accept(amount);
    }

    private static void checkBalance() {
        System.out.println("Your balance: $" + accountDAO.checkBalance(userAccountId));
    }

    private static void transferMoney() {
        System.out.print("Enter receiver's username: ");
        String receiverUsername = scanner.nextLine();
        executeAmountInput(amount -> {
            System.out.println(transactionService.transferMoney(userAccountId, receiverUsername, amount) ? "Transfer successful!" : "Transfer failed.");
        });
    }

    private static void checkDepositBalance() {
        if (depositAccountDAO.depositAccountExists(loggedInUserId)) {
            double depositBalance = depositAccountDAO.getDepositBalance(loggedInUserId);
            System.out.println("Your deposit account balance: " + depositBalance);
        } else {
            System.out.println("You don't have an open deposit account..");
        }
    }
    private static void transferFromDeposit() {
        if (!depositAccountDAO.depositAccountExists(loggedInUserId)) {
            System.out.println("You don't have an deposit account.");
            return;
        }

        System.out.print("Enter the amount to transfer fro1m the deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        double depositBalance = depositAccountDAO.getDepositBalance(loggedInUserId);
        if (amount <= 0) {
            System.out.println("The amount must be positive.");
            return;
        }
        if (amount > depositBalance) {
            System.out.println("Insufficient funds in the deposit account.");
            return;
        }


        depositAccountDAO.updateDepositBalance(loggedInUserId, depositBalance - amount);
        double mainBalance = userDAO.getBalance(loggedInUserId);
        userDAO.updateBalance(loggedInUserId, mainBalance + amount);

        System.out.println("The transfer was completed successfully. Your deposit has been withdrawn " + amount);
    }
    private static void logout() {
        System.out.println("Logging out...");
        loggedInUserId = -1;
        userAccountId = -1;
    }

    private static void manageUsers() {
        String sql = "SELECT id, username, role FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("ID | Username | Role");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("username") + " | " + rs.getString("role"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.print("Enter username of the user to delete: ");
        String username = scanner.nextLine();
        boolean isDeleted = userDAO.deleteUser(username); // you can use the same deleteUser method in UserDAO
        if (isDeleted) {
            System.out.println("User deleted successfully.");
        } else {
            System.out.println("Error while deleting the user.");
        }
    }


    public static void viewAllUsers() {
        String sql = "SELECT u.username, u.password, a.balance, a.account_type FROM users u JOIN accounts a ON u.id = a.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            System.out.println("--- All Users ---");
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password"); // You can choose to hide the password for security reasons
                double balance = rs.getDouble("balance");
                String accountType = rs.getString("account_type");

                // Print the user details
                System.out.println("Username: " + username + ", Password: " + password + ", Balance: " + balance + ", Account Type: " + accountType);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
    }

    private static void loginToClientAccount() {
        System.out.print("Enter client's username: ");
        String clientUsername = scanner.nextLine();

        // Check if the client exists
        int clientId = userDAO.getUserId(clientUsername);

        if (clientId == -1) {
            System.out.println("Client not found.");
            return;
        }

        System.out.println("Logged in as " + clientUsername + ". You can now manage the client's account.");

        System.out.println("--- Client Account Management ---");
        System.out.println("1. Get the Money");
        System.out.println("2. Withdraw Money to Deposit");
        System.out.println("3. Check Balance");
        System.out.println("4. Transfer Money");
        System.out.println("5. Logout");
        System.out.println("6. Check Deposit Account Balance"); // Added
        System.out.println("7. Transfer from Deposit to Main Account"); // Added
        System.out.println("8. Show Transaction History");
        // Option to go to client account management
        handleClientMenu();
    }

    private static void editClientInformation() {
        System.out.print("Enter client's username to edit: ");
        String clientUsername = scanner.nextLine();

        // Check if the client exists
        int clientId = userDAO.getUserId(clientUsername);

        if (clientId == -1) {
            System.out.println("Client not found.");
            return;
        }

        // Ask for new password
        System.out.print("Enter new password for " + clientUsername + ": ");
        String newPassword = scanner.nextLine();

        // Update the password in the database
        boolean success = userDAO.updatePassword(clientId, newPassword);
        if (success) {
            System.out.println("Password updated successfully.");
        } else {
            System.out.println("Failed to update password.");
        }
    }



    private static void showTransactionHistory() {
        transactionService.showTransactionHistory(userAccountId);
    }
}
