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

        if (userRole.equals("client")) {

            System.out.println("--- Client Account Management ---");
            System.out.println("1. Get the Money");
            System.out.println("2. Withdraw Money to Deposit");
            System.out.println("3. Check Balance");
            System.out.println("4. Transfer Money");
            System.out.println("5. Logout");
            System.out.println("6. Check Deposit Account Balance");
            System.out.println("7. Transfer from Deposit to Main Account");
            System.out.println("8. Show Transaction History");


            handleClientMenu();
        } else if (userRole.equals("manager")) {

            System.out.println("--- Manager Account Management ---");
            System.out.println("1. View All Users");
            System.out.println("2. Login to Client Account");
            System.out.println("3. Edit Client Information");
            System.out.println("4. Logout");


            handleManagerMenu();
        } else if (userRole.equals("admin")) {
            System.out.println("--- Admin Management ---");
            System.out.println("1. View All Users");
            System.out.println("2. Login to Client Account");
            System.out.println("3. Edit Client Information");
            System.out.println("4. Delete User");
            System.out.println("5. Logout");


            handleAdminMenu();
        } else {
            System.out.println("Invalid role! Please contact support.");
        }
    }


    private static void handleClientMenu() {
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                depositMoney();
                break;
            case 2:
                withdrawMoney();
                break;
            case 3:
                checkBalance();
                break;
            case 4:
                transferMoney();
                break;
            case 5:
                logout();
                break;
            case 6:
                checkDepositBalance();
                break;
            case 7:
                transferFromDeposit();
                break;
            case 8:
                showTransactionHistory();
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }


    private static void handleManagerMenu() {
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                viewAllUsers();
                break;
            case 2:
                loginToClientAccount();
                break;
            case 3:
                editClientInformation();
                break;
            case 4:
                logout();
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }


    private static void handleAdminMenu() {
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                viewAllUsers();
                break;
            case 2:
                loginToClientAccount();
                break;
            case 3:
                editClientInformation();
                break;
            case 4:
                deleteUser();
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


        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }


        if (isUsernameTaken(username)) {
            System.out.println("Username is already taken. Please choose a different username.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();


        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }



        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        if (!password.matches(passwordPattern)) {
            System.out.println("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one digit.");
            return;
        }

        System.out.print("Enter role (admin/manager/client): ");
        String role = scanner.nextLine();


        if (!(role.equals("admin") || role.equals("manager") || role.equals("client"))) {
            System.out.println("Invalid role! Please choose between 'admin', 'manager', or 'client'.");
            return;
        }


        boolean success = userDAO.registerUser(username, password, role);
        if (success) {
            System.out.println("User registered successfully!");
        } else {
            System.out.println("Registration failed.");
        }
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


            userRole = userDAO.getUserRole(username);
            System.out.println("You are logged in as: " + userRole);
        } else {
            System.out.println("Invalid username or password.");
        }
    }



    private static void depositMoney() {
        executeAmountInput(amount -> {
            if (amount <= 0) {
                System.out.println("Deposit amount must be greater than 0.");
            } else {
                System.out.println(transactionService.processDeposit(userAccountId, amount) ? "Deposit successful!" : "Deposit failed.");
            }
        });
    }

    private static void withdrawMoney() {
        executeAmountInput(amount -> {
            if (amount <= 0) {
                System.out.println("Withdraw amount must be greater than 0.");
            } else {
                double mainBalance = accountDAO.checkBalance(userAccountId);
                if (mainBalance < amount) {
                    System.out.println("Insufficient funds.");
                } else if (accountDAO.withdraw(userAccountId, amount)) {
                    System.out.println("Withdrawal successful.");
                } else {
                    System.out.println("Withdrawal failed.");
                }
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
            if (amount <= 0) {
                System.out.println("Transfer amount must be greater than 0.");
            } else {
                System.out.println(transactionService.transferMoney(userAccountId, receiverUsername, amount) ? "Transfer successful!" : "Transfer failed.");
            }
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

        System.out.print("Enter the amount to transfer from the deposit: ");
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
        boolean isDeleted = userDAO.deleteUser(username);
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
                String password = rs.getString("password");
                double balance = rs.getDouble("balance");
                String accountType = rs.getString("account_type");


                System.out.println("Username: " + username + ", Password: " + password + ", Balance: " + balance + ", Account Type: " + accountType);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
    }

    private static void loginToClientAccount() {
        System.out.print("Enter client's username: ");
        String clientUsername = scanner.nextLine();


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
        System.out.println("6. Check Deposit Account Balance");
        System.out.println("7. Transfer from Deposit to Main Account");
        System.out.println("8. Show Transaction History");

        handleClientMenu();
    }

    private static void editClientInformation() {
        System.out.print("Enter client's username to edit: ");
        String clientUsername = scanner.nextLine();


        int clientId = userDAO.getUserId(clientUsername);

        if (clientId == -1) {
            System.out.println("Client not found.");
            return;
        }


        System.out.print("Enter new password for " + clientUsername + ": ");
        String newPassword = scanner.nextLine();


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


    private static boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
        }
        return false;
    }
}
