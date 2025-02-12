package com.bankapp;

import java.util.Scanner;

public class BankApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionService transactionService = new TransactionService(accountDAO);
    private static final DepositAccountDAO depositAccountDAO = new DepositAccountDAO();

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
        System.out.println("--- Account Management ---");
        System.out.println("1. Get the Money");
        System.out.println("2. Withdraw Money to Deposit");
        System.out.println("3. Check Balance");
        System.out.println("4. Transfer Money");
        System.out.println("5. Logout");
        System.out.println("6. Check Deposit Account Balance");
        System.out.println("7. Transfer from Deposit to Main Account");
        System.out.println("8. Show transaction history");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        Runnable action = (choice == 1) ? BankApp::depositMoney :
                          (choice == 2) ? BankApp::withdrawMoney :
                          (choice == 3) ? BankApp::checkBalance :
                          (choice == 4) ? BankApp::transferMoney :
                          (choice == 5) ? BankApp::logout :
                          (choice == 6) ? BankApp::checkDepositBalance :
                          (choice == 7) ? BankApp::transferFromDeposit :
                          (choice == 8) ? BankApp::showTransactionHistory :
                          () -> System.out.println("Invalid choice.");
        action.run();
    }

    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.println(userDAO.registerUser(username, password) ? "User registered successfully!" : "Registration failed.");
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

    private static void showTransactionHistory() {
        transactionService.showTransactionHistory(userAccountId);
    }
}
