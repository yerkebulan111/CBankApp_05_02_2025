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
                System.out.println("1. Get the Money");
                System.out.println("2. Withdraw Money to Deposit");
                System.out.println("3. Check Balance");
                System.out.println("4. Transfer Money");
                System.out.println("5. Logout");
                System.out.println("6. Check Deposit Account Balance");
                System.out.println("7. Transfer from Deposit to Main Account");
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

            if (depositAccountDAO.depositAccountExists(loggedInUserId)) {
                depositAccountDAO.incrementLoginCountAndApplyBonus(loggedInUserId);
                double depBalance = depositAccountDAO.getDepositBalance(loggedInUserId);
                System.out.println("Your deposit account balance is: " + depBalance);
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
        System.out.print("Enter amount to Deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        double mainBalance = accountDAO.checkBalance(userAccountId);
        if (mainBalance < amount) {
            System.out.println("Insufficient funds in your main account.");
            return;
        }
        boolean withdrawn = accountDAO.withdraw(userAccountId, amount);
        if (!withdrawn) {
            System.out.println("Transfer failed.");
            return;
        }
        if (!depositAccountDAO.depositAccountExists(loggedInUserId)) {
            boolean opened = depositAccountDAO.openDepositAccount(loggedInUserId, amount);
            if (opened) {
                System.out.println("Deposit account opened with initial deposit: " + amount);
            } else {
                System.out.println("Failed to open deposit account.");
            }
        } else {
            boolean deposited = depositAccountDAO.depositToDepositAccount(loggedInUserId, amount);
            if (deposited) {
                System.out.println("Deposit account credited with: " + amount);
            } else {
                System.out.println("Failed to credit deposit account.");
            }
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

    private static void checkDepositBalance() {
        if (depositAccountDAO.depositAccountExists(loggedInUserId)) {
            double depositBalance = depositAccountDAO.getDepositBalance(loggedInUserId);
            System.out.println("Ваш баланс депозитного счета: " + depositBalance);
        } else {
            System.out.println("У вас нет открытого депозитного счета.");
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




    private static void transferMoney() {
        System.out.print("Enter receiver's username: ");
        String receiverUsername = scanner.nextLine();
        System.out.print("Enter amount to transfer: ");
        double amount = scanner.nextDouble();

        if (transactionService.transferMoney(userAccountId, receiverUsername, amount)) {
            System.out.println("Transfer successful!");
        } else {
            System.out.println("Transfer failed.");
        }
    }

    private static void logout() {
        System.out.println("Logging out...");
        loggedInUserId = -1;
        userAccountId = -1;
    }
}
