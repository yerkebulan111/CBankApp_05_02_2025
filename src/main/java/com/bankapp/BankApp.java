package com.bankapp;

import java.util.Scanner;

public class BankApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionService transactionService = new TransactionService(accountDAO);

    private static int loggedInUserId = -1;
    private static int userAccountId = -1;

    public static void main(String[] args) {
        while (true) {
            if (loggedInUserId == -1) {
                System.out.println("--- Main Menu ---");
                System.out.println("1. Register User");
                System.out.println("2. Login User");
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
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            } else {

                System.out.println("--- Account Management ---");
                System.out.println("1. Deposit Money");
                System.out.println("2. Withdraw Money");
                System.out.println("3. Check Balance");
                System.out.println("4. Logout");
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
                        logout();
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
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
            loggedInUserId = userDAO.getUserId(username);
            userAccountId = accountDAO.getAccountIdByUserId(loggedInUserId);
            if (userAccountId != -1) {
                System.out.println("Your account ID is: " + userAccountId);
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void depositMoney() {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        if (transactionService.processDeposit(userAccountId, amount)) {
            System.out.println("Deposit successful and transaction recorded!");
        } else {
            System.out.println("Deposit failed.");
        }
    }

    private static void withdrawMoney() {
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        if (transactionService.processWithdrawal(userAccountId, amount)) {
            System.out.println("Withdrawal successful and transaction recorded!");
        } else {
            System.out.println("Withdrawal failed.");
        }
    }

    private static void checkBalance() {
        double balance = accountDAO.checkBalance(userAccountId);
        if (balance != -1) {
            System.out.println("Your balance is: $" + balance);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void logout() {
        System.out.println("Logging out...");
        loggedInUserId = -1;
        userAccountId = -1;
    }
}
