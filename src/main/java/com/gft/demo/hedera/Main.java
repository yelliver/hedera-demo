package com.gft.demo.hedera;

import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenId;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        var mainAccountCred = AccountCred.fromEnv("MY_ACCOUNT_ID", "MY_PRIVATE_KEY");
        var accountClient = AccountClient.fromCred(mainAccountCred);

        AccountCred newAccountCred = null;
        var demoAccountCred = AccountCred.fromEnv("DEMO_ACCOUNT_A_ID", "DEMO_PRIVATE_A_KEY");
        var myTokenId = TokenId.fromString(Dotenv.load().get("MY_TOKEN_ID"));

        accountClient.createAccount(100);
        System.out.println("===========================");
        System.out.println(newAccountCred);

//        try (Scanner scanner = new Scanner(System.in)) {
//            int choice;
//            do {
//                int i = 1;
//                System.out.println("\n====================MENU====================");
//                System.out.printf("%d. Show main account%n", i++);
//                System.out.printf("%d. Show sub demo account%n", i++);
//                System.out.printf("%d. Show my token info%n", i++);
//
//                System.out.printf("%d. Query main account balance%n", i++);
//                System.out.printf("%d. Query demo account balance%n", i++);
//                System.out.printf("%d. Query new account balance%n", i++);
//
//                System.out.printf("%d. Transfer HBar to sub account%n", i++);
//                System.out.printf("%d. Transfer Token to sub account%n", i++);
//
//                System.out.printf("%d. Create new account%n", i++);
//                System.out.printf("%d. Transfer HBar to new account%n", i++);
//                System.out.printf("%d. Associate Token with new account%n", i++);
//                System.out.printf("%d. Transfer Token to new account%n", i++);
//                System.out.println("0. Exit");
//
//                System.out.print("Enter your choice: ");
//                choice = scanner.nextInt();
//
//                try {
//                    switch (choice) {
//                        case 1:
//                            System.out.println("Main Account: " + mainAccountCred);
//                            break;
//                        case 2:
//                            System.out.println("Sub Demo Account: " + demoAccountCred);
//                            break;
//                        case 3:
//                            var tokenInfo = accountClient.tokenInfo(myTokenId);
//                            System.out.println("My Token: " + tokenInfo);
//                            break;
//                        case 4:
//                            var mainAccountQuery = accountClient.balanceQuery();
//                            System.out.println("Main account balance: " + mainAccountQuery);
//                            break;
//                        case 5:
//                            var demoAccountQuery = accountClient.balanceQuery(demoAccountCred.getAccountId());
//                            System.out.println("Demo account balance: " + demoAccountQuery);
//                            break;
//                        case 6:
//                            var newAccountQuery = accountClient.balanceQuery(newAccountCred.getAccountId());
//                            System.out.println("New account balance: " + newAccountQuery);
//                            break;
//                        case 7:
//                            var statusHBarDemo = accountClient.sendTo(demoAccountCred.getAccountId(), 100);
//                            System.out.println("Transfer HBar result: " + statusHBarDemo);
//                            break;
//                        case 8:
//                            var statusTokenDemo = accountClient.sendTokenTo(demoAccountCred.getAccountId(), myTokenId, 100);
//                            System.out.println("Transfer Token result: " + statusTokenDemo);
//                            break;
//                        case 9:
//                            newAccountCred = accountClient.createAccount(100);
//                            System.out.println("Create account result: " + newAccountCred);
//                            break;
//                        case 10:
//                            var associateTokenResult = accountClient.associateToken(newAccountCred, myTokenId);
//                            System.out.println("Associate token result: " + associateTokenResult);
//                            break;
//                        case 11:
//                            var statusHBarNew = accountClient.sendTo(newAccountCred.getAccountId(), 100);
//                            System.out.println("Transfer HBar result: " + statusHBarNew);
//                            break;
//                        case 12:
//                            var statusTokenNew = accountClient.sendTokenTo(newAccountCred.getAccountId(), myTokenId, 100);
//                            System.out.println("Transfer Token result: " + statusTokenNew);
//                            break;
//                        case 0:
//                            System.out.println("Exiting...");
//                            break;
//                        default:
//                            System.out.println("Invalid choice. Please try again.");
//                    }
//                } catch (Exception e) {
//                    System.out.println("ERROR: " + e.getMessage());
//                }
//            } while (choice != 0);
//        }
    }
}