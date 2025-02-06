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
                System.out.println("1. Replenishment of debit account");
                System.out.println("2. Transfer money to another user");
                System.out.println("3. Check Balance");
                System.out.println("4. Credit");
                System.out.println("5. Deposit");
                System.out.println("6. Logout");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        replenishAccount();
                        break;
                    case 2:
                        transferMoney();
                        break;
                    case 3:
                        checkBalance();
                        break;
                    case 4:
                        handleCredit();
                        break;
                    case 5:
                        handleDeposit();
                        break;
                    case 6:
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
            System.out.println("User registered successfully.");
        } else {
            System.out.println("User registration failed.");
        }
    }

    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        loggedInUserId = userDAO.loginUser(username, password);

        if (loggedInUserId != -1) {
            System.out.println("Login successful.");
            userAccountId = accountDAO.getAccountIdByUserId(loggedInUserId);
            if (userAccountId == -1) {
                accountDAO.createAccount(loggedInUserId);
                userAccountId = accountDAO.getAccountIdByUserId(loggedInUserId);
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void replenishAccount() {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        if (transactionService.processDeposit(userAccountId, amount)) {
            System.out.println("Amount deposited successfully.");
        } else {
            System.out.println("Deposit failed.");
        }
    }

    private static void transferMoney() {
        System.out.print("Enter recipient username: ");
        String recipientUsername = scanner.nextLine();
        System.out.print("Enter amount to transfer: ");
        double amount = scanner.nextDouble();
        if (accountDAO.checkBalance(userAccountId) < amount) {
            System.out.println("Insufficient balance.");
            return;
        }
        if (transactionService.transferMoney(userAccountId, recipientUsername, amount)) {
            System.out.println("Transfer successful.");
        } else {
            System.out.println("Transfer failed.");
        }
    }

    private static void checkBalance() {
        double balance = accountDAO.checkBalance(userAccountId);
        System.out.println("Your balance is: " + balance + " ₸");
    }

    private static void handleCredit() {
        System.out.println("1. Check credit conditions");
        System.out.println("2. Apply for credit");
        System.out.print("Choose an option: ");
        int creditChoice = scanner.nextInt();
        scanner.nextLine();

        switch (creditChoice) {
            case 1:
                System.out.println("Check credit conditions");
                break;
            case 2:
                System.out.println("Applying for credit");
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    private static void handleDeposit() {
        System.out.println("1. Open a deposit account");
        System.out.println("2. View deposit balance");
        System.out.print("Choose an option: ");
        int depositChoice = scanner.nextInt();
        scanner.nextLine();

        switch (depositChoice) {
            case 1:
                System.out.println("Opening a deposit account");
                break;
            case 2:
                double depositBalance = accountDAO.checkDepositBalance(userAccountId);
                System.out.println("Deposit balance: " + depositBalance + " ₸");
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    private static void logout() {
        loggedInUserId = -1;
        userAccountId = -1;
        System.out.println("Logged out successfully.");
    }
}
