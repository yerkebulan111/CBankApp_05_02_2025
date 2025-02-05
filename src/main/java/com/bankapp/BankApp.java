package com.bankapp;

import java.util.Scanner;

public class BankApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final AccountDAO accountDAO = new AccountDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("--- User Management ---");
            System.out.println("1. Register User");
            System.out.println("2. Login User");
            System.out.println("3. Delete User");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    deleteUser();
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (userDAO.registerUser(username, password)) {
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
            manageAccount();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void deleteUser() {
        System.out.print("Enter user ID to delete: ");
        int userId = scanner.nextInt();
        if (userDAO.deleteUser(userId)) {
            System.out.println("User deleted successfully!");
        } else {
            System.out.println("User deletion failed.");
        }
    }

    private static void manageAccount() {
        while (true) {
            System.out.println("--- Account Management ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Delete Account");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    withdrawMoney();
                    break;
                case 4:
                    checkBalance();
                    break;
                case 5:
                    deleteAccount();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createAccount() {
        System.out.print("Enter your user ID: ");
        int userId = scanner.nextInt();
        if (accountDAO.createAccount(userId)) {
            System.out.println("Account created successfully!");
        } else {
            System.out.println("Account creation failed.");
        }
    }

    private static void depositMoney() {
        System.out.print("Enter account ID: ");
        int accountId = scanner.nextInt();
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        if (accountDAO.deposit(accountId, amount)) {
            System.out.println("Deposit successful and transaction recorded!");
        } else {
            System.out.println("Deposit failed.");
        }
    }

    private static void withdrawMoney() {
        System.out.print("Enter account ID: ");
        int accountId = scanner.nextInt();
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        if (accountDAO.withdraw(accountId, amount)) {
            System.out.println("Withdrawal successful and transaction recorded!");
        } else {
            System.out.println("Withdrawal failed.");
        }
    }

    private static void checkBalance() {
        System.out.print("Enter account ID: ");
        int accountId = scanner.nextInt();
        double balance = accountDAO.checkBalance(accountId);
        if (balance != -1) {
            System.out.println("Your balance is: $" + balance);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void deleteAccount() {
        System.out.print("Enter account ID to delete: ");
        int accountId = scanner.nextInt();
        if (accountDAO.deleteAccount(accountId)) {
            System.out.println("Account deleted successfully!");
        } else {
            System.out.println("Account deletion failed.");
        }
    }
}
